import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { PageHeaderComponent } from '../../shared/ui/page-header/page-header.component';
import { StatCardComponent } from '../../shared/ui/stat-card/stat-card.component';
import { ContentPanelComponent } from '../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';
import { AuthService } from '../../features/auth/data-access/auth.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatSelectModule,
    PageHeaderComponent,
    StatCardComponent,
    ContentPanelComponent,
    EmptyStateComponent,
  ],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss',
})
export class DashboardPageComponent {
  private readonly authService = inject(AuthService);

  readonly currentUser = this.authService.currentUser;

  readonly periodOptions = [
    { value: 'current-month', label: 'Este mês' },
    { value: 'previous-month', label: 'Mês anterior' },
    { value: 'three-months', label: 'Últimos 3 meses' },
    { value: 'six-months', label: 'Últimos 6 meses' },
    { value: 'current-year', label: 'Este ano' },
  ] as const;
  readonly selectedPeriod = signal<(typeof this.periodOptions)[number]['value']>('current-month');

  // Page header actions
  readonly headerActions = [
    {
      label: 'Nova transação',
      icon: 'add',
      handler: () => console.log('Nova transação'),
      tooltip: 'Adicionar nova transação',
    },
  ];

  readonly chartActions = [
    {
      label: 'Período',
      icon: 'date_range',
      handler: () => console.log('Selecionar período'),
      variant: 'flat' as const,
      color: 'primary' as const,
    },
  ];

  readonly transactionsActions = [
    {
      label: 'Ver todas',
      icon: 'list',
      handler: () => console.log('Ver todas transações'),
      variant: 'flat' as const,
      color: 'primary' as const,
    },
  ];

  // KPI Cards data (placeholder values)
  readonly kpiCards = [
    {
      label: 'Entradas',
      value: 'R$ 0,00',
      icon: 'trending_up',
      variant: 'income' as const,
      trend: '',
      trendPositive: true,
    },
    {
      label: 'Saídas',
      value: 'R$ 0,00',
      icon: 'trending_down',
      variant: 'expense' as const,
      trend: '',
      trendPositive: true,
    },
    {
      label: 'Saldo',
      value: 'R$ 0,00',
      icon: 'account_balance',
      variant: 'balance' as const,
      trend: '',
      trendPositive: true,
    },
    {
      label: 'Patrimônio',
      value: 'R$ 0,00',
      icon: 'savings',
      variant: 'asset' as const,
      trend: '',
      trendPositive: true,
    },
  ];

  // Recent transactions placeholder
  readonly recentTransactions = [
    {
      id: 1,
      description: 'Nenhuma transação registrada',
      category: '',
      account: '',
      date: '',
      value: '',
      type: 'empty' as 'empty' | 'income' | 'expense',
      icon: '',
    },
  ];

  onNewTransaction(): void {
    console.log('Nova transação');
  }

  onPeriodChange(value: (typeof this.periodOptions)[number]['value']): void {
    this.selectedPeriod.set(value);
  }
}