import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../features/auth/data-access/auth.service';

/**
 * Interceptor que trata erros 401 centralizados para endpoints autenticados.
 *
 * Comportamento:
 * - 401 em endpoint protegido: limpa sessão e redireciona para /login
 * - 401 em POST /api/auth/login: NÃO intercepta (deixa para tela de login)
 * - 403: NÃO limpa sessão (usuário autenticado sem permissão)
 * - Outros erros: propagados normalmente
 */
export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }

      // 401 Unauthorized
      if (error.status === 401) {
        const requestUrl = req.url.startsWith('http')
          ? req.url
          : `${location.origin}${req.url}`;

        // NÃO interceptar 401 de login - credenciais inválidas
        if (isLoginEndpoint(requestUrl)) {
          return throwError(() => error);
        }

        // NÃO interceptar 401 de register
        if (isRegisterEndpoint(requestUrl)) {
          return throwError(() => error);
        }

        // 401 em endpoint protegido -> limpar sessão e redirecionar
        // clearSession() é idempotente e seguro para chamadas simultâneas
        authService.clearSession();

        // Navega para login preservando returnUrl se possível
        const returnUrl = extractReturnUrl(requestUrl);
        const loginUrl = returnUrl
          ? router.createUrlTree(['/login'], { queryParams: { returnUrl } })
          : '/login';

        void router.navigateByUrl(loginUrl);
        return throwError(() => error);
      }

      // 403 Forbidden: NÃO limpar sessão automaticamente
      // Usuário autenticado pode simplesmente não ter permissão
      if (error.status === 403) {
        return throwError(() => error);
      }

      return throwError(() => error);
    })
  );
};

/**
 * Verifica se a URL é o endpoint de login
 */
function isLoginEndpoint(requestUrl: string): boolean {
  const apiBaseUrl = environment.apiUrl;
  const loginUrl = `${apiBaseUrl}/auth/login`;
  return requestUrl.startsWith(loginUrl);
}

/**
 * Verifica se a URL é o endpoint de register
 */
function isRegisterEndpoint(requestUrl: string): boolean {
  const apiBaseUrl = environment.apiUrl;
  const registerUrl = `${apiBaseUrl}/auth/register`;
  return requestUrl.startsWith(registerUrl);
}

/**
 * Extrai URL de retorno válida (apenas rotas internas)
 */
function extractReturnUrl(requestUrl: string): string | null {
  try {
    const apiBaseUrl = environment.apiUrl;
    // Remove a base URL da API para obter o path
    if (requestUrl.startsWith(apiBaseUrl)) {
      const path = requestUrl.slice(apiBaseUrl.length);
      // Remove leading slash e query parameters
      const cleanPath = path.split('?')[0].replace(/^\//, '');
      if (cleanPath && !cleanPath.startsWith('http')) {
        return '/' + cleanPath;
      }
    }
  } catch {
    // Ignora erros de parsing
  }
  return null;
}