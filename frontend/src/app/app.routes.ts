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
        path: 'goals',
        loadComponent: () => import('./features/goals/pages/goals.page').then((m) => m.GoalsPageComponent),
      },
      {
        path: 'recurrences',
        loadComponent: () => import('./features/recurrences/pages/recurrences.page').then((m) => m.RecurrencesPageComponent),
      },
      {
        path: 'financial-calendar',
        loadComponent: () => import('./features/financial-calendar/pages/financial-calendar.page').then((m) => m.FinancialCalendarPageComponent),
      },
      {
        path: 'budgets',
        loadComponent: () => import('./features/budgets/pages/budgets.page').then((m) => m.BudgetsPageComponent),
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
        path: 'tags',
        loadComponent: () =>
          import('./features/tags/pages/tags.page').then((m) => m.TagsPageComponent),
      },
      {
        path: 'expenses',
        loadComponent: () =>
          import('./features/expenses/pages/expenses.page').then((m) => m.ExpensesPageComponent),
      },
      {
        path: 'incomes',
        loadComponent: () =>
          import('./features/incomes/pages/incomes.page').then((m) => m.IncomesPageComponent),
      },
      {
        path: 'transfers',
        loadComponent: () =>
          import('./features/transfers/pages/transfers.page').then((m) => m.TransfersPageComponent),
      },
      {
        path: 'transactions',
        loadComponent: () =>
          import('./features/transactions/pages/transactions.page').then((m) => m.TransactionsPageComponent),
      },
      {
        path: 'reports/financial',
        loadComponent: () =>
          import('./features/reports/pages/financial-report.page').then((m) => m.FinancialReportPageComponent),
      },
      {
        path: 'reports/categories',
        loadComponent: () =>
          import('./features/reports/pages/category-report.page').then((m) => m.CategoryReportPageComponent),
      },
      {
        path: 'reports/audit',
        loadComponent: () =>
          import('./features/reports/pages/audit-report.page').then((m) => m.AuditReportPageComponent),
      },
    ],
  },
];