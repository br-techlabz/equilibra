import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DatePipe } from '@angular/common';
import { PageHeaderComponent } from '../../shared/ui/page-header/page-header.component';
import { StatCardComponent } from '../../shared/ui/stat-card/stat-card.component';
import { ContentPanelComponent } from '../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';
import { AuthService } from '../../features/auth/data-access/auth.service';
import { AssetAccountsApiService } from '../asset-accounts/data-access/asset-accounts-api.service';
import { AssetAccount, assetAccountTypeOption } from '../asset-accounts/models/asset-account.models';
import { DashboardApiService } from './dashboard-api.service';
import { DashboardData } from './dashboard.models';
import { formatCentsAsBRL, sumMoneyInCents } from './money.utils';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    FormsModule,
    MatIconModule,
    MatSelectModule,
    MatButtonModule,
    MatTooltipModule,
    PageHeaderComponent,
    StatCardComponent,
    ContentPanelComponent,
    EmptyStateComponent,
  ],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss',
})
export class DashboardPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly accountsApi = inject(AssetAccountsApiService);
  private readonly dashboardApi = inject(DashboardApiService);
  private readonly router = inject(Router);

  readonly currentUser = this.authService.currentUser;
  readonly accounts = signal<AssetAccount[]>([]);
  readonly dashboardData = signal<DashboardData | null>(null);
  readonly accountsLoaded = signal(false);
  readonly accountsLoading = signal(false);
  readonly accountsError = signal(false);

  readonly periodOptions = [
    { value: 'current-month', label: 'Este mês' },
    { value: 'previous-month', label: 'Mês anterior' },
    { value: 'three-months', label: 'Últimos 3 meses' },
    { value: 'six-months', label: 'Últimos 6 meses' },
    { value: 'current-year', label: 'Este ano' },
  ] as const;
  readonly selectedPeriod = signal<(typeof this.periodOptions)[number]['value']>('current-month');

  readonly headerActions = [
    {
      label: 'Nova transação',
      icon: 'add',
      handler: () => undefined,
      tooltip: 'Escolha o tipo de transação',
      menu: [
        { label: 'Despesa', icon: 'trending_down', handler: () => this.router.navigate(['/expenses']) },
        { label: 'Receita', icon: 'trending_up', handler: () => this.router.navigate(['/incomes']) },
        { label: 'Transferência', icon: 'swap_horiz', handler: () => this.router.navigate(['/transfers']) },
      ],
    },
  ];
  readonly chartActions = [];
  readonly transactionsActions = [];
  readonly recentTransactions: never[] = [];

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.accountsLoading.set(true);
    const { from, to } = this.periodRange(this.selectedPeriod());
    this.dashboardApi.get(from.toISOString(), to.toISOString()).subscribe({
      next: (data) => {
        this.dashboardData.set(data);
        this.accounts.set(data.accounts.map((account) => ({ id: account.accountId, name: account.name, type: account.type as AssetAccount['type'], initialBalance: account.currentBalance, active: account.active, createdAt: '', updatedAt: '' })));
        this.accountsLoaded.set(true); this.accountsLoading.set(false); this.accountsError.set(false);
      },
      error: () => { this.accountsLoading.set(false); this.accountsError.set(true); },
    });
  }

  loadAccounts(): void {
    this.accountsLoading.set(true);
    this.accountsLoaded.set(false);
    this.accountsError.set(false);
    this.accountsApi.list(false).subscribe({
      next: (accounts) => {
        this.accounts.set(accounts.filter((account) => account.active));
        this.accountsLoaded.set(true);
        this.accountsLoading.set(false);
      },
      error: () => {
        this.accounts.set([]);
        this.accountsLoading.set(false);
        this.accountsError.set(true);
      },
    });
  }

  navigateToAccounts(): void {
    this.router.navigate(['/accounts']);
  }

  onPeriodChange(value: (typeof this.periodOptions)[number]['value']): void {
    this.selectedPeriod.set(value);
    this.loadDashboard();
  }

  private periodRange(period: (typeof this.periodOptions)[number]['value']): { from: Date; to: Date } {
    const now = new Date();
    const to = new Date(now);
    let from: Date;

    switch (period) {
      case 'previous-month':
        from = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        to.setTime(new Date(now.getFullYear(), now.getMonth(), 1).getTime());
        break;
      case 'three-months':
        from = new Date(now.getFullYear(), now.getMonth() - 2, 1);
        break;
      case 'six-months':
        from = new Date(now.getFullYear(), now.getMonth() - 5, 1);
        break;
      case 'current-year':
        from = new Date(now.getFullYear(), 0, 1);
        break;
      default:
        from = new Date(now.getFullYear(), now.getMonth(), 1);
    }

    return { from, to };
  }

  formatMoney(value: number | string): string {
    return formatCentsAsBRL(sumMoneyInCents([value]));
  }

  transactionAmount(amount: number | string | null | undefined): string {
    const value = Number(amount);
    return Number.isFinite(value) ? new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value) : 'R$ 0,00';
  }

  formatCents(cents: number): string {
    return formatCentsAsBRL(cents);
  }

  get netWorthCents(): number {
    return sumMoneyInCents(this.accounts().map((account) => account.initialBalance));
  }

  accountType(account: AssetAccount) {
    return assetAccountTypeOption(account.type);
  }

  currentMonthDays(): string[] {
    const periodEnd = this.dashboardData()?.period.to;
    const today = new Date();
    const end = periodEnd ? new Date(periodEnd) : today;
    const isCurrentMonth = end.getFullYear() === today.getFullYear() && end.getMonth() === today.getMonth();
    const totalDays = isCurrentMonth
      ? today.getDate()
      : new Date(end.getFullYear(), end.getMonth() + 1, 0).getDate();

    return Array.from({ length: totalDays }, (_, index) => String(index + 1).padStart(2, '0'));
  }

  chartYAxis(): string[] {
    const values = this.dashboardData()?.balanceEvolution.flatMap(series => series.points.map(point => Number(point.balance))) ?? [];
    if (!values.length) return ['R$ 0,00'];
    const min = Math.min(...values); const max = Math.max(...values); const step = (max - min || Math.max(Math.abs(max), 1)) / 4;
    return [max, max - step, max - step * 2, max - step * 3, min].map(value => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 }).format(value));
  }

  balanceLinePoints(points: Array<{ at: string; balance: number }>): string {
    if (!points.length) return '';
    const visibleDays = this.currentMonthDays().length;
    const visiblePoints = points.slice(0, visibleDays);
    const values = visiblePoints.map(point => Number(point.balance));
    const min = Math.min(...values); const max = Math.max(...values); const range = max - min || 1;
    return visiblePoints.map((point, index) => `${4 + (index / Math.max(visiblePoints.length - 1, 1)) * 92},${88 - ((Number(point.balance) - min) / range) * 76}`).join(' ');
  }
}
