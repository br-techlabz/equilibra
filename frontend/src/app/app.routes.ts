import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full',
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/pages/login/login.page').then((m) => m.LoginPageComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/pages/register/register.page').then((m) => m.RegisterPageComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.page').then((m) => m.DashboardPageComponent),
      },
      {
        path: 'accounts',
        loadComponent: () =>
          import('./features/asset-accounts/pages/asset-accounts.page').then((m) => m.AssetAccountsPageComponent),
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./features/categories/pages/categories.page').then((m) => m.CategoriesPageComponent),
      },
      {
        path: 'expenses',
        loadComponent: () =>
          import('./features/expenses/pages/expenses.page').then((m) => m.ExpensesPageComponent),
      },
    ],
  },
];