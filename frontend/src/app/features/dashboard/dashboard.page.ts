import { Component, OnInit, inject, signal } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageHeaderComponent } from '../../shared/ui/page-header/page-header.component';
import { StatCardComponent } from '../../shared/ui/stat-card/stat-card.component';
import { ContentPanelComponent } from '../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';
import { AuthService } from '../../features/auth/data-access/auth.service';
import { AssetAccountsApiService } from '../asset-accounts/data-access/asset-accounts-api.service';
import { AssetAccount, assetAccountTypeOption } from '../asset-accounts/models/asset-account.models';
import { DashboardApiService } from './dashboard-api.service'; import { FinancialPredictabilityService } from './financial-predictability.service'; import { FinancialPredictabilityData } from './financial-predictability.models';
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
    BaseChartDirective,
  ],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss',
})
export class DashboardPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly accountsApi = inject(AssetAccountsApiService);
  private readonly dashboardApi = inject(DashboardApiService); private readonly predictabilityApi = inject(FinancialPredictabilityService);
  private readonly router = inject(Router);

  readonly currentUser = this.authService.currentUser;
  readonly accounts = signal<AssetAccount[]>([]);
  readonly dashboardData = signal<DashboardData | null>(null); readonly predictability = signal<FinancialPredictabilityData | null>(null); readonly predictabilityLoading = signal(false); readonly predictabilityError = signal(false);
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
  readonly balanceChartData: ChartData<'line'> = { labels: [], datasets: [] };
  readonly balanceChartOptions: ChartConfiguration<'line'>['options'] = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false }, tooltip: { callbacks: { label: context => `${context.dataset.label}: ${this.formatMoney(Number(context.raw))}` } } }, scales: { x: { grid: { display: false }, ticks: { display: false } }, y: { beginAtZero: true, grid: { color: 'rgba(128,128,128,.2)' } } } };

  // Pie chart data for expenses by category
  readonly expensePieChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  readonly expensePieChartOptions: ChartConfiguration<'doughnut'>['options'] = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false }, tooltip: { callbacks: { label: context => `${context.label}: ${this.formatMoney(Number(context.raw))}` } } }, cutout: '60%' };

  // Pie chart data for income by category
  readonly incomePieChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  readonly incomePieChartOptions: ChartConfiguration<'doughnut'>['options'] = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false }, tooltip: { callbacks: { label: context => `${context.label}: ${this.formatMoney(Number(context.raw))}` } } }, cutout: '60%' };

  // Paleta de cores do Design System (valores hex reais para Chart.js)
  readonly chartColorPalette = [
    { border: '#7c3aed', bg: '#f5f3ff', legend: 'legend-0' },     // primary-500 / primary-50
    { border: '#4caf50', bg: '#e8f5e9', legend: 'legend-1' },      // success-500 / success-50
    { border: '#e54848', bg: '#fdf2f2', legend: 'legend-2' },      // danger-500 / danger-50
    { border: '#ffc107', bg: '#fff8e1', legend: 'legend-3' },      // warning-500 / warning-50
    { border: '#2196f3', bg: '#e3f2fd', legend: 'legend-4' },      // info-500 / info-50
  ];

  // Mapeia accountId para índice de cor consistente
  private accountColorMap = new Map<string, number>();
  private nextColorIndex = 0;

  private getColorIndexForAccount(accountId: string): number {
    if (this.accountColorMap.has(accountId)) {
      return this.accountColorMap.get(accountId)!;
    }
    // Atribuir cores sequencialmente para as primeiras 5 contas (sem repetição)
    // Se já usamos todas as 5 cores, usar hash determinístico para as demais
    let index: number;
    if (this.nextColorIndex < this.chartColorPalette.length) {
      index = this.nextColorIndex;
      this.nextColorIndex++;
    } else {
      // Hash simples determinístico do accountId para contas extras
      let hash = 0;
      for (let i = 0; i < accountId.length; i++) {
        hash = ((hash << 5) - hash) + accountId.charCodeAt(i);
        hash |= 0;
      }
      index = Math.abs(hash) % this.chartColorPalette.length;
    }
    this.accountColorMap.set(accountId, index);
    return index;
  }

  // Reset do contador de cores ao recarregar dashboard
  private resetColorAssignment(): void {
    this.accountColorMap.clear();
    this.nextColorIndex = 0;
  }

  ngAfterViewInit(): void {
    // As cores já são definidas como hex no loadDashboard
  }

  ngOnInit(): void {
    this.loadDashboard(); this.loadPredictability();
  }

  loadPredictability(): void { const { from, to } = this.periodRange(this.selectedPeriod()); this.predictabilityLoading.set(true); this.predictabilityError.set(false); this.predictabilityApi.load(from.toISOString().slice(0,10),to.toISOString().slice(0,10)).subscribe({next:value=>{this.predictability.set(value);this.predictabilityLoading.set(false)},error:()=>{this.predictabilityLoading.set(false);this.predictabilityError.set(true)}}); }
  loadDashboard(): void {
    this.resetColorAssignment();
    this.accountsLoading.set(true);
    const { from, to } = this.periodRange(this.selectedPeriod());
    this.dashboardApi.get(from.toISOString(), to.toISOString()).subscribe({
      next: (data) => {
        this.dashboardData.set(data);
        this.balanceChartData.labels = data.balanceEvolution[0]?.points.map(point => point.at) ?? [];
        this.balanceChartData.datasets = data.balanceEvolution.map((series) => {
          const colorIndex = this.getColorIndexForAccount(series.accountId);
          const color = this.chartColorPalette[colorIndex];
          return {
            label: series.accountName,
            data: series.points.map(point => Number(point.balance)),
            borderColor: color.border,
            backgroundColor: 'transparent',
            tension: .25,
            pointRadius: 2,
            fill: false,
            className: `balance-line-${colorIndex}`,
          };
        });
        // Populate expense pie chart
        const expenseItems = data.expenseByCategory ?? [];
        this.expensePieChartData.labels = expenseItems.map(item => item.categoryName);
        this.expensePieChartData.datasets = [{
          data: expenseItems.map(item => Number(item.amount)),
          backgroundColor: expenseItems.map((_, index) => this.chartColorPalette[index % this.chartColorPalette.length].border),
          borderWidth: 0,
        }];
        // Populate income pie chart
        const incomeItems = data.incomeByCategory ?? [];
        this.incomePieChartData.labels = incomeItems.map(item => item.categoryName);
        this.incomePieChartData.datasets = [{
          data: incomeItems.map(item => Number(item.amount)),
          backgroundColor: incomeItems.map((_, index) => this.chartColorPalette[index % this.chartColorPalette.length].border),
          borderWidth: 0,
        }];
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

  navigateToCashFlow(): void { this.router.navigate(['/cash-flow']); }
  navigateToCalendar(): void { this.router.navigate(['/financial-calendar']); }
  navigateToAccounts(): void {
    this.router.navigate(['/accounts']);
  }

  onPeriodChange(value: (typeof this.periodOptions)[number]['value']): void {
    this.selectedPeriod.set(value);
    this.loadDashboard(); this.loadPredictability();
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

  isCreditCardTransaction(transaction: DashboardData['recentTransactions'][number]): boolean {
    return transaction.paymentMethod === 'CREDIT_CARD'
      || transaction.type === 'STATEMENT_PAYMENT'
      || transaction.description.toLowerCase().includes('pagamento de fatura');
  }

  transactionIcon(transaction: DashboardData['recentTransactions'][number]): string {
    if (this.isCreditCardTransaction(transaction)) return 'credit_card';
    if (transaction.type === 'INCOME') return 'trending_up';
    if (transaction.type === 'EXPENSE') return 'trending_down';
    return 'swap_horiz';
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

  getLegendClassForAccount(accountId: string): string {
    const colorIndex = this.getColorIndexForAccount(accountId);
    return `legend-${colorIndex}`;
  }
}
