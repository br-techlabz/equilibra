import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AssetAccountsApiService } from '../../asset-accounts/data-access/asset-accounts-api.service';
import { AssetAccount } from '../../asset-accounts/models/asset-account.models';
import { CategoriesApiService } from '../../categories/data-access/categories-api.service';
import { Category } from '../../categories/models/category.models';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { ExpensesApiService } from '../data-access/expenses-api.service';
import { Expense, ExpenseRequest } from '../models/expense.models';
import { TagsApiService } from '../../tags/data-access/tags-api.service';
import { Tag } from '../../tags/models/tag.models';
import { TagSelectorComponent } from '../../../shared/ui/tag-selector/tag-selector.component';

interface ExpenseForm { description: FormControl<string>; occurredAt: FormControl<string>; accountId: FormControl<string>; amount: FormControl<string>; categoryId: FormControl<string>; notes: FormControl<string>; }

@Component({
  selector: 'app-expenses-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule, MatPaginatorModule, MatSelectModule, MatSnackBarModule, ContentPanelComponent, EmptyStateComponent, PageHeaderComponent, TagSelectorComponent],
  template: `
    <main class="expenses-page">
      <app-page-header title="Despesas" subtitle="Registre e acompanhe seus gastos." [actions]="pageActions" />
      <app-content-panel title="Despesas" subtitle="Suas despesas registradas no ledger financeiro.">
        <div class="expenses-toolbar">
          <mat-checkbox [checked]="includeCancelled()" (change)="setIncludeCancelled($event.checked)">Mostrar despesas canceladas</mat-checkbox>
          @if (page()) { <span class="expense-count">{{ totalElements() }} {{ totalElements() === 1 ? 'despesa' : 'despesas' }}</span> }
        </div>
        @if (isLoading()) {
          <div class="state-block" role="status" aria-live="polite"><mat-icon class="spin" aria-hidden="true">sync</mat-icon><span>Carregando despesas...</span></div>
        } @else if (errorMessage()) {
          <div class="state-block error-state" role="alert"><mat-icon aria-hidden="true">error_outline</mat-icon><p>{{ errorMessage() }}</p><button mat-stroked-button type="button" (click)="loadExpenses()">Tentar novamente</button></div>
        } @else if (expenses().length === 0) {
          <app-empty-state icon="receipt_long" iconVariant="inbox" [title]="includeCancelled() ? 'Nenhuma despesa encontrada' : 'Nenhuma despesa registrada'" [description]="includeCancelled() ? 'Não há despesas para exibir.' : 'Registre sua primeira despesa para começar a acompanhar seus gastos.'" [centered]="false" [actions]="emptyActions" />
        } @else {
          <div class="desktop-table-wrapper"><table class="expenses-table"><thead><tr><th>Data</th><th>Descrição</th><th>Conta</th><th>Categoria</th><th>Valor</th><th>Status</th><th><span class="sr-only">Ações</span></th></tr></thead><tbody>
            @for (expense of expenses(); track expense.id) { <tr [class.cancelled-row]="expense.status === 'CANCELLED'"><td>{{ formatDate(expense.occurredAt) }}</td><td class="description-cell">{{ expense.description }}</td><td>{{ accountName(expense.accountId) }}</td><td>{{ categoryName(expense.categoryId) }}</td><td class="amount-cell">-{{ formatMoney(expense.amount) }}</td><td><span class="status-badge" [class.cancelled]="expense.status === 'CANCELLED'">{{ statusLabel(expense.status) }}</span></td><td class="actions-cell"><button mat-icon-button [matMenuTriggerFor]="expenseMenu" [matMenuTriggerData]="{ expense }" [attr.aria-label]="'Ações da despesa ' + expense.description"><mat-icon>more_vert</mat-icon></button></td></tr> }
          </tbody></table></div>
          <div class="mobile-expense-list">@for (expense of expenses(); track expense.id) { <article class="expense-card" [class.cancelled-row]="expense.status === 'CANCELLED'"><div class="expense-card-header"><div><strong>{{ expense.description }}</strong><span>{{ formatDate(expense.occurredAt) }}</span></div><button mat-icon-button [matMenuTriggerFor]="expenseMenu" [matMenuTriggerData]="{ expense }" [attr.aria-label]="'Ações da despesa ' + expense.description"><mat-icon>more_vert</mat-icon></button></div><div class="expense-card-meta"><span>{{ accountName(expense.accountId) }}</span><span>{{ categoryName(expense.categoryId) }}</span></div><div class="expense-card-footer"><strong>-{{ formatMoney(expense.amount) }}</strong><span class="status-badge" [class.cancelled]="expense.status === 'CANCELLED'">{{ statusLabel(expense.status) }}</span></div></article> }</div>
          @if (totalPages() > 1) { <mat-paginator [length]="totalElements()" [pageIndex]="page()" [pageSize]="pageSize" [pageSizeOptions]="[10, 20, 50]" (page)="onPageChange($event)" aria-label="Paginação de despesas" /> }
        }
      </app-content-panel>
      @if (isFormOpen()) { <button class="drawer-backdrop" type="button" aria-label="Fechar formulário" (click)="closeForm()"></button><aside class="expense-drawer" role="dialog" aria-modal="true" aria-labelledby="expense-drawer-title"><header class="drawer-header"><div><span class="eyebrow">Despesas</span><h2 id="expense-drawer-title">{{ editingExpense() ? 'Editar despesa' : 'Nova despesa' }}</h2></div><button mat-icon-button type="button" aria-label="Fechar formulário" (click)="closeForm()"><mat-icon>close</mat-icon></button></header><form class="expense-form" [formGroup]="form" (ngSubmit)="saveExpense()" novalidate><mat-form-field appearance="outline"><mat-label>Descrição</mat-label><input matInput formControlName="description" placeholder="Ex.: Supermercado" />@if (form.controls.description.hasError('required')) { <mat-error>Informe a descrição.</mat-error> }@if (form.controls.description.hasError('maxlength')) { <mat-error>Use no máximo 255 caracteres.</mat-error> }</mat-form-field><mat-form-field appearance="outline"><mat-label>Data e hora</mat-label><input matInput type="datetime-local" formControlName="occurredAt" />@if (form.controls.occurredAt.hasError('required')) { <mat-error>Informe a data e hora.</mat-error> }</mat-form-field><mat-form-field appearance="outline"><mat-label>Conta</mat-label><mat-select formControlName="accountId">@for (account of accounts(); track account.id) { <mat-option [value]="account.id">{{ account.name }}</mat-option> }</mat-select>@if (form.controls.accountId.hasError('required')) { <mat-error>Escolha uma conta.</mat-error> }</mat-form-field><mat-form-field appearance="outline"><mat-label>Valor</mat-label><input matInput inputmode="decimal" formControlName="amount" placeholder="0,00" /><mat-hint>Use até duas casas decimais.</mat-hint>@if (form.controls.amount.hasError('required') || form.controls.amount.hasError('pattern')) { <mat-error>Informe um valor positivo com até duas casas.</mat-error> }</mat-form-field><mat-form-field appearance="outline"><mat-label>Categoria</mat-label><mat-select formControlName="categoryId">@for (category of categories(); track category.id) { <mat-option [value]="category.id">{{ category.name }}</mat-option> }</mat-select>@if (form.controls.categoryId.hasError('required')) { <mat-error>Escolha uma categoria.</mat-error> }</mat-form-field><app-tag-selector [tags]="tags()" [value]="selectedTagIds()" (changed)="selectedTagIds.set($event)" /><mat-form-field appearance="outline"><mat-label>Observações</mat-label><textarea matInput rows="4" formControlName="notes"></textarea></mat-form-field>@if (formError()) { <p class="form-error" role="alert"><mat-icon aria-hidden="true">error_outline</mat-icon>{{ formError() }}</p> }<div class="drawer-actions"><button mat-button type="button" (click)="closeForm()">Cancelar</button><button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || isSaving()">{{ editingExpense() ? 'Salvar alterações' : 'Registrar despesa' }}</button></div></form></aside> }
      <mat-menu #expenseMenu="matMenu"><ng-template matMenuContent let-expense="expense"><button mat-menu-item [disabled]="expense.status === 'CANCELLED'" (click)="openEditForm(expense)"><mat-icon>edit</mat-icon><span>Editar</span></button><button mat-menu-item [disabled]="expense.status === 'CANCELLED'" (click)="cancelExpense(expense)"><mat-icon>cancel</mat-icon><span>Cancelar despesa</span></button></ng-template></mat-menu>
    </main>
  `,
  styleUrl: './expenses.page.scss',
})
export class ExpensesPageComponent implements OnInit {
  private readonly api = inject(ExpensesApiService); private readonly accountsApi = inject(AssetAccountsApiService); private readonly categoriesApi = inject(CategoriesApiService); private readonly tagsApi = inject(TagsApiService); private readonly snackBar = inject(MatSnackBar);
  readonly expenses = signal<Expense[]>([]); readonly tags = signal<Tag[]>([]); readonly selectedTagIds = signal<string[]>([]); readonly accounts = signal<AssetAccount[]>([]); readonly categories = signal<Category[]>([]); readonly isLoading = signal(false); readonly errorMessage = signal<string | null>(null); readonly includeCancelled = signal(false); readonly page = signal(0); readonly totalElements = signal(0); readonly totalPages = signal(0); readonly isFormOpen = signal(false); readonly editingExpense = signal<Expense | null>(null); readonly isSaving = signal(false); readonly formError = signal<string | null>(null); readonly pageSize = 20;
  readonly form: FormGroup<ExpenseForm>;
  readonly pageActions = [{ label: 'Nova despesa', icon: 'add', handler: () => this.openCreateForm() }]; readonly emptyActions = [{ label: 'Nova despesa', icon: 'add', handler: () => this.openCreateForm() }];
  constructor(private readonly formBuilder: NonNullableFormBuilder) {
    this.form = this.formBuilder.group({
      description: this.formBuilder.control('', [Validators.required, Validators.maxLength(255)]),
      occurredAt: this.formBuilder.control('', Validators.required),
      accountId: this.formBuilder.control('', Validators.required),
      amount: this.formBuilder.control('', [Validators.required, Validators.pattern(/^\d+(?:[,.]\d{1,2})?$/)]),
      categoryId: this.formBuilder.control('', Validators.required),
      notes: this.formBuilder.control('', Validators.maxLength(4000)),
    });
  }
  ngOnInit(): void { this.loadOptions(); this.tagsApi.list(false).subscribe({next: value => this.tags.set(value)}); this.loadExpenses(); }
  loadOptions(): void { this.accountsApi.list(false).subscribe({ next: (v) => this.accounts.set(v), error: () => undefined }); this.categoriesApi.list(false, 'EXPENSE').subscribe({ next: (v) => this.categories.set(v), error: () => undefined }); }
  loadExpenses(): void { this.isLoading.set(true); this.errorMessage.set(null); this.api.list(this.page(), this.pageSize, this.includeCancelled()).subscribe({ next: (v) => { this.expenses.set(v.content); this.totalElements.set(v.totalElements); this.totalPages.set(v.totalPages); this.isLoading.set(false); }, error: () => { this.isLoading.set(false); this.errorMessage.set('Não foi possível carregar suas despesas.'); } }); }
  setIncludeCancelled(value: boolean): void { this.includeCancelled.set(value); this.page.set(0); this.loadExpenses(); }
  onPageChange(event: PageEvent): void { this.page.set(event.pageIndex); this.loadExpenses(); }
  accountName(id: string): string { return this.accounts().find((v) => v.id === id)?.name ?? 'Conta indisponível'; }
  categoryName(id: string): string { return this.categories().find((v) => v.id === id)?.name ?? 'Categoria indisponível'; }
  formatDate(value: string): string { const d = new Date(value); return Number.isNaN(d.getTime()) ? '—' : new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(d); }
  formatMoney(value: number): string { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value); }
  statusLabel(value: Expense['status']): string { return value === 'CANCELLED' ? 'Cancelada' : 'Ativa'; }
  openCreateForm(): void { this.editingExpense.set(null); this.formError.set(null); this.selectedTagIds.set([]); this.form.reset({ description: '', occurredAt: this.localDateTime(new Date().toISOString()), accountId: '', amount: '', categoryId: '', notes: '' }); this.isFormOpen.set(true); }
  openEditForm(expense: Expense): void { this.editingExpense.set(expense); this.formError.set(null); this.selectedTagIds.set((expense.tags ?? []).map(tag => tag.id)); this.form.reset({ description: expense.description, occurredAt: this.localDateTime(expense.occurredAt), accountId: expense.accountId, amount: expense.amount.toFixed(2).replace('.', ','), categoryId: expense.categoryId, notes: expense.notes ?? '' }); this.isFormOpen.set(true); }
  closeForm(): void { if (!this.isSaving()) this.isFormOpen.set(false); }
  saveExpense(): void { if (this.form.invalid || this.isSaving()) { this.form.markAllAsTouched(); return; } const v = this.form.getRawValue(); const request: ExpenseRequest = { description: v.description, occurredAt: new Date(v.occurredAt).toISOString(), accountId: v.accountId, amount: Number(v.amount.replace('.', '').replace(',', '.')), categoryId: v.categoryId, notes: v.notes || null, tagIds: this.selectedTagIds() }; const current = this.editingExpense(); this.isSaving.set(true); this.formError.set(null); const op = current ? this.api.update(current.id, request) : this.api.create(request); op.subscribe({ next: () => { this.isSaving.set(false); this.isFormOpen.set(false); this.snackBar.open(current ? 'Despesa atualizada com sucesso.' : 'Despesa registrada com sucesso.', 'Fechar', { duration: 3500 }); this.loadExpenses(); }, error: (e: HttpErrorResponse) => { this.isSaving.set(false); this.formError.set(e.status === 409 ? 'A conta ou categoria não pode ser usada nesta despesa.' : e.status === 404 ? 'A conta, categoria ou despesa não está disponível.' : 'Não foi possível concluir a operação. Tente novamente.'); } }); }
  cancelExpense(expense: Expense): void { if (!window.confirm('Cancelar esta despesa? Ela permanecerá no histórico, mas deixará de afetar os cálculos financeiros.')) return; this.api.cancel(expense.id).subscribe({ next: () => { this.snackBar.open('Despesa cancelada com sucesso.', 'Fechar', { duration: 3500 }); this.loadExpenses(); }, error: () => this.snackBar.open('Não foi possível cancelar a despesa.', 'Fechar', { duration: 3500 }) }); }
  private localDateTime(iso: string): string { const d = new Date(iso); const pad = (n: number) => String(n).padStart(2, '0'); return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`; }
}
