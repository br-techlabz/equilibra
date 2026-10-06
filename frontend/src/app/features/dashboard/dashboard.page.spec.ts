import { of, throwError } from 'rxjs';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AssetAccountsApiService } from '../asset-accounts/data-access/asset-accounts-api.service';
import { DashboardPageComponent } from './dashboard.page';
import { formatCentsAsBRL, sumMoneyInCents } from './money.utils';
import { AuthService } from '../auth/data-access/auth.service';
import { DashboardApiService } from './dashboard-api.service';

describe('DashboardPageComponent', () => {
  const accountsApi = jasmine.createSpyObj<AssetAccountsApiService>('AssetAccountsApiService', ['list']);
  const dashboardApi = jasmine.createSpyObj('DashboardApiService', ['get']);
  const router = jasmine.createSpyObj<Router>('Router', ['navigate']);
  const authService = { currentUser: () => null } as unknown as AuthService;

  beforeEach(() => {
    accountsApi.list.and.returnValue(of([]));
    dashboardApi.get.and.returnValue(of({ period: { from: '', to: '' }, summary: { income: 0, expense: 0, net: 0, netWorth: 0 }, accounts: [], recentTransactions: [] }));
    TestBed.configureTestingModule({
      imports: [DashboardPageComponent],
      providers: [
        { provide: AssetAccountsApiService, useValue: accountsApi },
        { provide: AuthService, useValue: authService },
        { provide: DashboardApiService, useValue: dashboardApi },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('calculates money in cents safely', () => {
    expect(sumMoneyInCents([0, 0])).toBe(0);
    expect(sumMoneyInCents([100.1, 200.2])).toBe(30030);
    expect(sumMoneyInCents([0.1, 0.2])).toBe(30);
    expect(sumMoneyInCents([1000, -350.5])).toBe(64950);
    expect(sumMoneyInCents([-100, -200])).toBe(-30000);
    expect(formatCentsAsBRL(30030)).toBe('R$ 300,30');
  });

  it('loads only active accounts and calculates initial net worth', () => {
    dashboardApi.get.and.returnValue(of({ period: { from: '', to: '' }, summary: { income: 0, expense: 0, net: 0, netWorth: 300 }, accounts: [{ accountId: '1', name: 'A', type: 'CASH', initialBalance: 100, currentBalance: 300, active: true }], recentTransactions: [] }));
    accountsApi.list.and.returnValue(of([
      { id: '1', name: 'A', type: 'CASH', initialBalance: 100, active: true, createdAt: '', updatedAt: '' },
      { id: '2', name: 'B', type: 'CASH', initialBalance: 200, active: true, createdAt: '', updatedAt: '' },
      { id: '3', name: 'C', type: 'CASH', initialBalance: 500, active: false, createdAt: '', updatedAt: '' },
    ]));
    const fixture = TestBed.createComponent(DashboardPageComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(dashboardApi.get).toHaveBeenCalled();
    expect(component.accounts().length).toBe(1);
    expect(component.dashboardData()?.summary.netWorth).toBe(300);
    expect(component.accountsError()).toBeFalse();
  });

  it('represents API errors without confirming zero patrimoine and retries', () => {
    dashboardApi.get.and.returnValue(throwError(() => new Error('offline')));
    const fixture = TestBed.createComponent(DashboardPageComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.accountsError()).toBeTrue();
    expect(fixture.componentInstance.accountsLoaded()).toBeFalse();
    dashboardApi.get.and.returnValue(of({ period: { from: '', to: '' }, summary: { income: 0, expense: 0, net: 0, netWorth: 0 }, accounts: [], recentTransactions: [] }));
    fixture.componentInstance.loadDashboard();
    expect(fixture.componentInstance.accountsError()).toBeFalse();
    expect(fixture.componentInstance.accountsLoaded()).toBeTrue();
  });
});
