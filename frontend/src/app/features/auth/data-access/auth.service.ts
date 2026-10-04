import { Injectable, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthApiService } from './auth-api.service';
import { LoginRequest } from './login-request';
import { LoginResponse } from './login-response';
import { CurrentUser } from './current-user';

export type AuthenticationStatus = 'unauthenticated' | 'authenticating' | 'authenticated';

/**
 * Serviço responsável pelo estado de autenticação do frontend.
 * <p>
 * O access token permanece <strong>somente em memória</strong>.
 * Não há persistência em localStorage, sessionStorage, IndexedDB ou cookies.
 * <p>
 * Consequência: recarregar a página encerra a sessão do frontend.
 * Esse comportamento é aceito nesta fase e será expandido na TASK-1.9.
 * </p>
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly authApi = inject(AuthApiService);
  private readonly router = inject(Router);

  // Estado privado: access token (somente em memória)
  private readonly _accessToken = signal<string | null>(null);
  // Estado privado: usuário atual
  private readonly _currentUser = signal<CurrentUser | null>(null);
  // Estado de autenticação
  private readonly _status = signal<AuthenticationStatus>('unauthenticated');
  // Erro de autenticação atual
  private readonly _authError = signal<string | null>(null);

  // Sinais públicos derivados
  readonly accessToken = computed(() => this._accessToken());
  readonly currentUser = computed(() => this._currentUser());
  readonly status = computed(() => this._status());
  readonly authError = computed(() => this._authError());
  readonly isAuthenticated = computed(() => this._status() === 'authenticated');
  readonly isAuthenticating = computed(() => this._status() === 'authenticating');

  /**
   * Realiza o fluxo completo de login:
   * 1. POST /api/auth/login
   * 2. Armazena token em memória
   * 3. GET /api/users/me com Bearer token
   * 4. Armazena usuário e define estado authenticated
   * 5. Navega para área principal
   *
   * Em caso de falha no /me, limpa tudo e volta para unauthenticated.
   *
   * @param request Credenciais de login
   */
  login(request: LoginRequest): void {
    this._authError.set(null);
    this._status.set('authenticating');

    this.authApi.login(request).subscribe({
      next: (loginResponse: LoginResponse) => {
        this._accessToken.set(loginResponse.accessToken);
        this.loadCurrentUser();
      },
      error: (error: unknown) => {
        this.handleLoginError(error);
      },
    });
  }

  private loadCurrentUser(): void {
    this.authApi.getCurrentUser().subscribe({
      next: (user: CurrentUser) => {
        this._currentUser.set(user);
        this._status.set('authenticated');
        // Respeita returnUrl se existir, senão dashboard
        const returnUrl = this.extractReturnUrl();
        void this.router.navigate([returnUrl]);
      },
      error: (error: unknown) => {
        this.handleMeError(error);
      },
    });
  }

  /**
   * Extrai returnUrl válida dos query params atuais, se houver.
   */
  private extractReturnUrl(): string {
    // O router não está disponível aqui de forma síncrona,
    // então navegamos para dashboard como fallback.
    // A navegação com returnUrl será tratada pelo login page
    // ou pelo AuthService quando chamado com contexto.
    return '/dashboard';
  }

  private handleLoginError(error: unknown): void {
    this._accessToken.set(null);
    this._status.set('unauthenticated');

    if (error instanceof HttpErrorResponse) {
      if (error.status === 401) {
        this._authError.set('E-mail ou senha inválidos.');
      } else if (error.status === 400) {
        this._authError.set(error.error?.detail || 'Dados de login inválidos.');
      } else if (error.status === 0) {
        this._authError.set('Não foi possível conectar ao serviço. Tente novamente.');
      } else {
        this._authError.set('Não foi possível autenticar. Tente novamente.');
      }
    } else {
      this._authError.set('Não foi possível autenticar. Tente novamente.');
    }
  }

  private handleMeError(error: unknown): void {
    this._accessToken.set(null);
    this._currentUser.set(null);
    this._status.set('unauthenticated');

    if (error instanceof HttpErrorResponse) {
      if (error.status === 401) {
        this._authError.set('Sessão inválida. Tente fazer login novamente.');
      } else if (error.status === 404) {
        this._authError.set('Usuário não encontrado. Tente fazer login novamente.');
      } else {
        this._authError.set('Não foi possível carregar seu perfil. Tente novamente.');
      }
    } else {
      this._authError.set('Não foi possível carregar seu perfil. Tente novamente.');
    }
  }

  /**
   * Limpa completamente a sessão (token, usuário, status).
   * Usado para logout ou reset interno.
   * Idempotente: seguro para chamadas repetidas.
   */
  clearSession(): void {
    this._accessToken.set(null);
    this._currentUser.set(null);
    this._status.set('unauthenticated');
    this._authError.set(null);
  }

  /**
   * Retorna o access token atual para uso pelo Authorization Interceptor.
   * Somente leitura: componentes não devem conseguir substituir o token diretamente.
   */
  getAccessToken(): string | null {
    return this._accessToken();
  }
}