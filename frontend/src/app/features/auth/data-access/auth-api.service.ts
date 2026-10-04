import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { RegisterRequest } from './register-request';
import { RegisterResponse } from './register-response';
import { LoginRequest } from './login-request';
import { LoginResponse } from './login-response';
import { CurrentUser } from './current-user';

/**
 * Serviço para operações de autenticação/registro contra a API backend.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /**
   * Registra um novo usuário.
   *
   * @param request Dados de cadastro (email e senha)
   * @returns Observable com a resposta do cadastro
   */
  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/auth/register`, request);
  }

  /**
   * Autentica usuário via email e senha.
   *
   * @param request Credenciais (email e senha)
   * @returns Observable com o access token
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, request);
  }

  /**
   * Obtém o usuário autenticado atual.
   * O Authorization header é adicionado automaticamente pelo authTokenInterceptor.
   *
   * @returns Observable com os dados do usuário atual
   */
  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${this.apiUrl}/users/me`);
  }
}