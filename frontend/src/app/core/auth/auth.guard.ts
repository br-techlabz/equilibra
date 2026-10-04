import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../../features/auth/data-access/auth.service';

/**
 * Guard para rotas que exigem autenticação.
 *
 * Se autenticado: permite navegação (true)
 * Se não autenticado: redireciona para /login com returnUrl (UrlTree)
 */
export const authGuard: CanActivateFn = (route, state): boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Preserva a rota desejada como returnUrl para redirecionar após login
  const returnUrl = state.url;
  const validReturnUrl = isValidInternalUrl(returnUrl)
    ? returnUrl
    : '/dashboard';

  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: validReturnUrl },
  });
};

/**
 * Valida se a URL é uma rota interna da aplicação (não open redirect)
 */
function isValidInternalUrl(url: string): boolean {
  if (!url || url.startsWith('http') || url.startsWith('//')) {
    return false;
  }
  return true;
}