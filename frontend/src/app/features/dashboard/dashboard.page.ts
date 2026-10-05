import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
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
    MatIconModule,
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
      trend: '+12,5%',
      trendPositive: true,
    },
    {
      label: 'Saídas',
      value: 'R$ 0,00',
      icon: 'trending_down',
      variant: 'expense' as const,
      trend: '+8,2%',
      trendPositive: false,
    },
    {
      label: 'Saldo',
      value: 'R$ 0,00',
      icon: 'account_balance',
      variant: 'balance' as const,
      trend: 'R$ 0,00',
      trendPositive: true,
    },
    {
      label: 'Patrimônio',
      value: 'R$ 0,00',
      icon: 'savings',
      variant: 'asset' as const,
      trend: '+5,3%',
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
}