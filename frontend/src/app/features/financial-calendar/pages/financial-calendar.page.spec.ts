import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FinancialCalendarPageComponent } from './financial-calendar.page';

describe('FinancialCalendarPageComponent', () => {
  let fixture: ComponentFixture<FinancialCalendarPageComponent>;
  let component: FinancialCalendarPageComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [FinancialCalendarPageComponent], providers: [provideHttpClient(), provideHttpClientTesting()] }).compileComponents();
    fixture = TestBed.createComponent(FinancialCalendarPageComponent);
    component = fixture.componentInstance;
  });

  it('creates the civil day range for the selected month', () => {
    component.month.set('2026-02');
    expect(component.range()).toEqual({ from: '2026-02-01', to: '2026-02-28' });
    expect(component.days()).toHaveSize(28);
  });

  it('counts commitments by due date and selects a day', () => {
    component.items.set([
      { id: '1', type: 'EXPENSE', description: 'Internet', plannedAmount: 129.9, dueDate: '2026-10-10', accountId: 'a', categoryId: 'c', recurrenceRuleId: null, status: 'PENDING', financialTransactionId: null, settledAt: null, overdue: false, createdAt: '', updatedAt: '' },
      { id: '2', type: 'INCOME', description: 'Salário', plannedAmount: 3500, dueDate: '2026-10-10', accountId: 'a', categoryId: 'c', recurrenceRuleId: null, status: 'SETTLED', financialTransactionId: 'tx', settledAt: '', overdue: false, createdAt: '', updatedAt: '' },
    ]);
    expect(component.count('2026-10-10')).toBe(2);
    component.selectDay('2026-10-10');
    expect(component.visible()).toHaveSize(2);
  });
});
