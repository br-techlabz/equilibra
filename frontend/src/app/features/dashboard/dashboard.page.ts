import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
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
import { formatCentsAsBRL, sumMoneyInCents } from './money.utils';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
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
  private readonly router = inject(Router);

  readonly currentUser = this.authService.currentUser;
  readonly accounts = signal<AssetAccount[]>([]);
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
    { label: 'Nova transação', icon: 'add', handler: () => undefined, tooltip: 'Disponível quando transações forem implementadas' },
  ];
  readonly chartActions = [];
  readonly transactionsActions = [];
  readonly recentTransactions: never[] = [];

  ngOnInit(): void {
    this.loadAccounts();
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
  }

  formatMoney(value: number | string): string {
    return formatCentsAsBRL(sumMoneyInCents([value]));
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
}
