import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../../features/auth/data-access/auth.service';

/**
 * Guard para rotas exclusivas de usuários NÃO autenticados (login, register).
 *
 * Se NÃO autenticado: permite navegação (true)
 * Se autenticado: redireciona para área principal (UrlTree)
 */
export const guestGuard: CanActivateFn = (): boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  // Usuário autenticado tentando acessar login/register
  // Redireciona para dashboard (ou returnUrl se houver)
  return router.createUrlTree(['/dashboard']);
};