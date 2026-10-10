import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { FinancialCommitmentApiService } from '../data-access/financial-commitment-api.service';
import { AssetAccountsApiService } from '../../asset-accounts/data-access/asset-accounts-api.service';
import { CategoriesApiService } from '../../categories/data-access/categories-api.service';
import { AssetAccount } from '../../asset-accounts/models/asset-account.models';
import { Category } from '../../categories/models/category.models';
import { CommitmentStatus, CommitmentType, FinancialCommitment } from '../models/financial-commitment.models';

@Component({
  selector: 'app-financial-calendar-page', standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule, MatSnackBarModule, PageHeaderComponent, ContentPanelComponent, EmptyStateComponent],
  templateUrl: './financial-calendar.page.html', styleUrl: './financial-calendar.page.scss',
})
export class FinancialCalendarPageComponent implements OnInit {
  private readonly api = inject(FinancialCommitmentApiService);
  private readonly accountsApi = inject(AssetAccountsApiService);
  private readonly categoriesApi = inject(CategoriesApiService);
  private readonly fb = inject(FormBuilder);
  private readonly snack = inject(MatSnackBar);
  readonly items = signal<FinancialCommitment[]>([]); readonly accounts = signal<AssetAccount[]>([]); readonly categories = signal<Category[]>([]); readonly loading = signal(false); readonly loaded = signal(false); readonly error = signal('');
  readonly month = signal(this.currentMonth()); readonly selectedDay = signal<string | null>(null); readonly typeFilter = signal<CommitmentType | 'ALL'>('ALL'); readonly statusFilter = signal<CommitmentStatus | 'ALL'>('ALL'); readonly editing = signal<FinancialCommitment | null>(null); readonly drawer = signal(false); readonly saving = signal(false);
  readonly pageActions = [{ label: 'Novo compromisso', icon: 'add', handler: () => this.openCreate() }];
  readonly form = this.fb.nonNullable.group({ type: ['EXPENSE' as CommitmentType, Validators.required], description: ['', Validators.required], plannedAmount: ['', Validators.required], dueDate: ['', Validators.required], accountId: ['', Validators.required], categoryId: ['', Validators.required] });
  readonly visible = computed(() => this.items().filter(x => (this.typeFilter() === 'ALL' || x.type === this.typeFilter()) && (this.statusFilter() === 'ALL' || x.status === this.statusFilter()) && (!this.selectedDay() || x.dueDate === this.selectedDay())));
  ngOnInit(): void { this.accountsApi.list(false).subscribe({ next: value => this.accounts.set(value), error: () => undefined }); this.categoriesApi.list(false).subscribe({ next: value => this.categories.set(value), error: () => undefined }); this.load(); }
  currentMonth(): string { return new Date().toISOString().slice(0, 7); }
  range(): { from: string; to: string } { const [y, m] = this.month().split('-').map(Number); return { from: `${this.month()}-01`, to: `${y}-${String(m).padStart(2, '0')}-${String(new Date(y, m, 0).getDate()).padStart(2, '0')}` }; }
  load(): void { this.loading.set(true); this.loaded.set(false); this.api.list({ ...this.range(), type: this.typeFilter() === 'ALL' ? undefined : this.typeFilter() as CommitmentType, status: this.statusFilter() === 'ALL' ? undefined : this.statusFilter() as CommitmentStatus }).subscribe({ next: r => { this.items.set(r.content); this.loading.set(false); this.loaded.set(true); }, error: () => { this.error.set('Não foi possível carregar a agenda.'); this.loading.set(false); this.loaded.set(true); } }); }
  changeMonth(delta: number): void { const [y, m] = this.month().split('-').map(Number); const d = new Date(y, m - 1 + delta, 1); this.month.set(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`); this.selectedDay.set(null); this.load(); }
  monthLabel(): string { const [y, m] = this.month().split('-').map(Number); return new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(new Date(y, m - 1, 1)); }
  weekdays(): string[] { return ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom']; }
  leadingDays(): number { const [y, m] = this.month().split('-').map(Number); return (new Date(y, m - 1, 1).getDay() + 6) % 7; }
  days(): string[] { const [y, m] = this.month().split('-').map(Number); return Array.from({ length: new Date(y, m, 0).getDate() }, (_, i) => `${this.month()}-${String(i + 1).padStart(2, '0')}`); }
  isToday(day: string): boolean { return day === new Date().toISOString().slice(0, 10); }
  count(day: string): number { return this.items().filter(x => x.dueDate === day).length; }
  selectDay(day: string): void { this.selectedDay.set(this.selectedDay() === day ? null : day); }
  openCreate(): void { this.editing.set(null); this.form.reset({ type: 'EXPENSE', description: '', plannedAmount: '', dueDate: `${this.month()}-01`, accountId: '', categoryId: '' }); this.drawer.set(true); }
  openEdit(x: FinancialCommitment): void { this.editing.set(x); this.form.reset({ type: x.type, description: x.description, plannedAmount: String(x.plannedAmount), dueDate: x.dueDate, accountId: x.accountId, categoryId: x.categoryId }); this.drawer.set(true); }
  close(): void { if (!this.saving()) this.drawer.set(false); }
  submit(): void { if (this.form.invalid || this.saving()) return; const v = this.form.getRawValue(); this.saving.set(true); const amount = Number(v.plannedAmount.replace(',', '.')); const edit = this.editing(); const call = edit ? this.api.update(edit.id, { description: v.description, plannedAmount: amount, dueDate: v.dueDate, accountId: v.accountId, categoryId: v.categoryId }) : this.api.create({ type: v.type, description: v.description, plannedAmount: amount, dueDate: v.dueDate, accountId: v.accountId, categoryId: v.categoryId, recurrenceRuleId: null }); call.subscribe({ next: () => { this.saving.set(false); this.drawer.set(false); this.load(); }, error: () => { this.saving.set(false); this.error.set('Não foi possível salvar o compromisso.'); } }); }
  cancel(x: FinancialCommitment): void { if (!confirm('Cancelar este compromisso?')) return; this.api.cancel(x.id).subscribe({ next: () => this.load(), error: () => this.error.set('Não foi possível cancelar o compromisso.') }); }
  settle(x: FinancialCommitment): void { const input = prompt('Valor efetivado', String(x.plannedAmount)); const amount = Number(input?.replace(',', '.')); if (!Number.isFinite(amount) || amount <= 0) return; this.api.settle(x.id, { amount, occurredAt: new Date().toISOString() }).subscribe({ next: () => { this.snack.open('Compromisso efetivado.', 'Fechar', { duration: 3000 }); this.load(); }, error: () => this.error.set('Não foi possível efetivar o compromisso.') }); }
  money(value: number | string): string { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value)); }
  status(value: CommitmentStatus): string { return { PENDING: 'Pendente', SETTLED: 'Efetivado', CANCELLED: 'Cancelado' }[value]; }
  type(value: CommitmentType): string { return value === 'EXPENSE' ? 'Despesa' : 'Receita'; }
}
