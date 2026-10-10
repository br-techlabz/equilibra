import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AssetAccountsApiService } from '../../asset-accounts/data-access/asset-accounts-api.service';
import { AssetAccount } from '../../asset-accounts/models/asset-account.models';
import { CategoriesApiService } from '../../categories/data-access/categories-api.service';
import { Category } from '../../categories/models/category.models';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { RecurrenceApiService } from '../data-access/recurrence-api.service';
import { RECURRENCE_FREQUENCIES, RECURRENCE_TYPES, Recurrence, RecurrenceFrequency, RecurrenceStatus, RecurrenceType } from '../models/recurrence.models';

@Component({
  selector: 'app-recurrences-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule, MatSelectModule, MatSnackBarModule, PageHeaderComponent, ContentPanelComponent, EmptyStateComponent],
  templateUrl: './recurrences.page.html',
  styleUrl: './recurrences.page.scss',
})
export class RecurrencesPageComponent implements OnInit {
  private readonly api = inject(RecurrenceApiService);
  private readonly categoriesApi = inject(CategoriesApiService);
  private readonly accountsApi = inject(AssetAccountsApiService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly items = signal<Recurrence[]>([]); readonly categories = signal<Category[]>([]); readonly accounts = signal<AssetAccount[]>([]);
  readonly loading = signal(false); readonly loaded = signal(false); readonly saving = signal(false); readonly formOpen = signal(false);
  readonly error = signal(''); readonly editing = signal<Recurrence | null>(null);
  readonly typeFilter = signal<RecurrenceType | 'ALL'>('ALL'); readonly statusFilter = signal<RecurrenceStatus | 'ALL'>('ALL');
  readonly typeOptions = RECURRENCE_TYPES; readonly frequencyOptions = RECURRENCE_FREQUENCIES;
  readonly statuses: ReadonlyArray<{ value: RecurrenceStatus | 'ALL'; label: string }> = [
    { value: 'ALL', label: 'Todos os estados' }, { value: 'ACTIVE', label: 'Ativas' }, { value: 'PAUSED', label: 'Pausadas' }, { value: 'ENDED', label: 'Encerradas' }, { value: 'CANCELLED', label: 'Canceladas' },
  ];
  readonly form = this.fb.nonNullable.group({ type: ['EXPENSE' as RecurrenceType, Validators.required], description: ['', [Validators.required, Validators.maxLength(255)]], amount: ['', [Validators.required, Validators.pattern(/^\d+(?:[,.]\d{1,2})?$/)]], categoryId: ['', Validators.required], accountId: ['', Validators.required], frequency: ['MONTHLY' as RecurrenceFrequency, Validators.required], startDate: ['', Validators.required], endDate: [''], dueDay: [1, [Validators.required, Validators.min(1), Validators.max(31)]] });
  readonly pageActions = [{ label: 'Nova recorrência', icon: 'add', handler: () => this.openCreate() }];

  ngOnInit(): void { this.loadReferences(); this.load(); }
  loadReferences(): void { this.categoriesApi.list(false).subscribe({ next: v => this.categories.set(v), error: () => this.error.set('Não foi possível carregar categorias.') }); this.accountsApi.list(false).subscribe({ next: v => this.accounts.set(v), error: () => this.error.set('Não foi possível carregar contas.') }); }
  load(): void { this.loading.set(true); this.loaded.set(false); this.api.list().subscribe({ next: v => { this.items.set(v); this.loading.set(false); this.loaded.set(true); }, error: () => { this.error.set('Não foi possível carregar suas recorrências.'); this.loading.set(false); this.loaded.set(true); } }); }
  visible(): Recurrence[] { return this.items().filter(x => (this.typeFilter() === 'ALL' || x.type === this.typeFilter()) && (this.statusFilter() === 'ALL' || x.status === this.statusFilter())); }
  openCreate(): void { this.editing.set(null); this.error.set(''); this.form.reset({ type: 'EXPENSE', description: '', amount: '', categoryId: '', accountId: '', frequency: 'MONTHLY', startDate: '', endDate: '', dueDay: 1 }); this.formOpen.set(true); }
  openEdit(item: Recurrence): void { this.editing.set(item); this.error.set(''); this.form.reset({ type: item.type, description: item.description, amount: this.amountText(item.amount), categoryId: item.categoryId, accountId: item.accountId, frequency: item.frequency, startDate: item.startDate, endDate: item.endDate ?? '', dueDay: item.dueDay }); this.formOpen.set(true); }
  closeForm(): void { if (!this.saving()) this.formOpen.set(false); }
  submit(): void { if (this.form.invalid || this.saving()) { this.form.markAllAsTouched(); return; } const v = this.form.getRawValue(); const amount = Number(v.amount.replace(',', '.')); if (!Number.isFinite(amount)) return; this.saving.set(true); const current = this.editing(); const call = current ? this.api.update(current.id, { description: v.description, amount, endDate: v.endDate || null, dueDay: v.dueDay }) : this.api.create({ type: v.type, description: v.description, amount, categoryId: v.categoryId, accountId: v.accountId, frequency: v.frequency, startDate: v.startDate, endDate: v.endDate || null, dueDay: v.dueDay }); call.subscribe({ next: () => { this.saving.set(false); this.formOpen.set(false); this.snackBar.open(current ? 'Recorrência atualizada.' : 'Recorrência criada.', 'Fechar', { duration: 3000 }); this.load(); }, error: error => { this.saving.set(false); this.error.set(error.status === 409 ? 'A operação não é permitida para o estado atual.' : 'Não foi possível salvar a recorrência.'); } }); }
  transition(item: Recurrence, action: 'pause' | 'resume' | 'end' | 'cancel'): void { const labels = { pause: 'pausar', resume: 'reativar', end: 'encerrar', cancel: 'cancelar' }; if (!window.confirm(`Deseja ${labels[action]} esta recorrência?`)) return; this.api[action](item.id).subscribe({ next: () => { this.snackBar.open('Estado da recorrência atualizado.', 'Fechar', { duration: 3000 }); this.load(); }, error: () => this.error.set('Não foi possível atualizar o estado da recorrência.') }); }
  description(id: string): string { return this.categories().find(x => x.id === id)?.name ?? id; }
  accountName(id: string): string { return this.accounts().find(x => x.id === id)?.name ?? id; }
  amountText(value: number | string): string { return Number(value).toFixed(2).replace('.', ','); }
  money(value: number | string): string { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value)); }
  typeLabel(value: RecurrenceType): string { return value === 'EXPENSE' ? 'Despesa' : 'Receita'; }
  frequencyLabel(value: RecurrenceFrequency): string { return value === 'MONTHLY' ? 'Mensal' : 'Anual'; }
  statusLabel(value: RecurrenceStatus): string { return { ACTIVE: 'Ativa', PAUSED: 'Pausada', ENDED: 'Encerrada', CANCELLED: 'Cancelada' }[value]; }
}
