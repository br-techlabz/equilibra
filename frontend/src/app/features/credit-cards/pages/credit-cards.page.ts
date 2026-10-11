import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CreditCardsApiService } from '../data-access/credit-cards-api.service';
import { CreditCard, CreditCardRequest, CreditCardStatement } from '../models/credit-card.models';
import { AssetAccountsApiService } from '../../asset-accounts/data-access/asset-accounts-api.service';
import { AssetAccount } from '../../asset-accounts/models/asset-account.models';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';

interface CreditCardForm {
  name: FormControl<string>;
  institution: FormControl<string>;
  nickname: FormControl<string>;
  totalLimit: FormControl<string>;
  closingDay: FormControl<number>;
  dueDay: FormControl<number>;
  timezone: FormControl<string>;
}

@Component({
  selector: 'app-credit-cards-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCheckboxModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule, MatProgressSpinnerModule, MatSelectModule, MatSnackBarModule,
    ContentPanelComponent, EmptyStateComponent, PageHeaderComponent],
  template: `
    <main class="credit-cards-page">
      <app-page-header title="Cartões de crédito" subtitle="Acompanhe seus limites e ciclos de pagamento."
        [actions]="pageActions" />
      <app-content-panel title="Meus cartões" subtitle="Cartões cadastrados no seu perfil.">
        <div class="cards-toolbar">
          <mat-checkbox [checked]="includeInactive()" (change)="setIncludeInactive($event.checked)">
            Mostrar cartões inativos
          </mat-checkbox>
          <span class="card-count">{{ cards().length }} {{ cards().length === 1 ? 'cartão' : 'cartões' }}</span>
        </div>
        @if (isLoading()) {
          <div class="loading" role="status"><mat-spinner diameter="36" /><span>Carregando cartões...</span></div>
        } @else if (loadError()) {
          <div class="error-state" role="alert"><mat-icon aria-hidden="true">error_outline</mat-icon><p>{{ loadError() }}</p><button mat-stroked-button type="button" (click)="loadCards()">Tentar novamente</button></div>
        } @else if (cards().length === 0) {
          <app-empty-state icon="credit_card" iconVariant="folder" title="Nenhum cartão cadastrado" description="Cadastre seu primeiro cartão de crédito para acompanhar seu limite." [centered]="false" [actions]="emptyActions" />
        } @else {
          <div class="card-grid" role="list">
            @for (card of cards(); track card.id) {
              <article (click)="openStatements(card)" tabindex="0" (keydown.enter)="openStatements(card)" class="credit-card" role="listitem" [class.inactive]="card.status === 'INACTIVE'">
                <div class="card-head"><span class="card-icon" aria-hidden="true"><mat-icon>credit_card</mat-icon></span><div class="identity"><strong>{{ card.name }}</strong><span>{{ card.institution }}{{ card.nickname ? ' · ' + card.nickname : '' }}</span></div><button mat-icon-button [matMenuTriggerFor]="cardMenu" [matMenuTriggerData]="{ card }" [attr.aria-label]="'Ações do cartão ' + card.name"><mat-icon>more_vert</mat-icon></button></div>
                <div class="limit-block"><span class="field-label">Limite disponível</span><strong>{{ formatMoney(card.availableLimit) }}</strong><div class="limit-meta"><span>Total {{ formatMoney(card.totalLimit) }}</span><span>Comprometido {{ formatMoney(card.committedLimit) }}</span></div></div>
                <div class="card-details"><div><span class="field-label">Fechamento</span><strong>Dia {{ card.closingDay }}</strong></div><div><span class="field-label">Vencimento</span><strong>Dia {{ card.dueDay }}</strong></div><span class="status" [class.status-inactive]="card.status === 'INACTIVE'"><span class="status-dot" aria-hidden="true"></span>{{ card.status === 'ACTIVE' ? 'Ativo' : 'Inativo' }}</span></div>
              </article>
            }
          </div>
        }
      </app-content-panel>
      @if (isFormOpen()) {
        <button class="drawer-backdrop" type="button" aria-label="Fechar formulário" (click)="closeForm()"></button>
        <aside class="card-drawer" role="dialog" aria-modal="true" aria-labelledby="drawer-title"><header class="drawer-header"><div><span class="eyebrow">Cartões de crédito</span><h2 id="drawer-title">{{ editingCard() ? 'Editar cartão' : 'Novo cartão' }}</h2></div><button mat-icon-button type="button" aria-label="Fechar formulário" (click)="closeForm()"><mat-icon>close</mat-icon></button></header>
          <form class="card-form" [formGroup]="form" (ngSubmit)="saveCard()" novalidate>
            <mat-form-field appearance="outline"><mat-label>Nome do cartão</mat-label><input matInput formControlName="name" autocomplete="off" /> @if (form.controls.name.hasError('required')) { <mat-error>Informe o nome do cartão.</mat-error> } @if (form.controls.name.hasError('maxlength')) { <mat-error>Use no máximo 100 caracteres.</mat-error> }</mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Instituição</mat-label><input matInput formControlName="institution" autocomplete="organization" /> @if (form.controls.institution.hasError('required')) { <mat-error>Informe a instituição.</mat-error> }</mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Apelido (opcional)</mat-label><input matInput formControlName="nickname" autocomplete="off" /></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Limite total</mat-label><input matInput formControlName="totalLimit" inputmode="decimal" placeholder="0,00" /><mat-hint>Valor positivo, com até duas casas decimais.</mat-hint>@if (form.controls.totalLimit.hasError('required')) { <mat-error>Informe o limite.</mat-error> } @if (form.controls.totalLimit.hasError('pattern')) { <mat-error>Informe um valor válido.</mat-error> }</mat-form-field>
            <div class="day-fields"><mat-form-field appearance="outline"><mat-label>Dia de fechamento</mat-label><input matInput type="number" formControlName="closingDay" min="1" max="31" /> @if (form.controls.closingDay.invalid && form.controls.closingDay.touched) { <mat-error>Use um dia entre 1 e 31.</mat-error> }</mat-form-field><mat-form-field appearance="outline"><mat-label>Dia de vencimento</mat-label><input matInput type="number" formControlName="dueDay" min="1" max="31" /> @if (form.controls.dueDay.invalid && form.controls.dueDay.touched) { <mat-error>Use um dia entre 1 e 31.</mat-error> }</mat-form-field></div>
            <mat-form-field appearance="outline"><mat-label>Fuso horário</mat-label><input matInput formControlName="timezone" autocomplete="off" /><mat-hint>Ex.: America/Sao_Paulo</mat-hint></mat-form-field>
            @if (formError()) { <p class="form-error" role="alert"><mat-icon aria-hidden="true">error_outline</mat-icon>{{ formError() }}</p> }
            <div class="drawer-actions"><button mat-button type="button" (click)="closeForm()">Cancelar</button><button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || isSaving()">{{ editingCard() ? 'Salvar alterações' : 'Cadastrar cartão' }}</button></div>
          </form>
        </aside>
      }
      @if (selectedCard()) { <button class="drawer-backdrop" type="button" aria-label="Fechar faturas" (click)="closeStatements()"></button><aside class="card-drawer statement-drawer" role="dialog" aria-modal="true" aria-labelledby="statement-title"><header class="drawer-header"><div><span class="eyebrow">{{ selectedCard()?.name }}</span><h2 id="statement-title">Faturas</h2></div><button mat-icon-button type="button" aria-label="Fechar faturas" (click)="closeStatements()"><mat-icon>close</mat-icon></button></header>@if (statementLoading()) { <div class="loading" role="status">Carregando faturas...</div> } @else if (statements().length === 0) { <p class="empty-statements">Nenhuma fatura disponível.</p> } @else { @for (statement of statements(); track statement.id) { <section class="statement-card"><div class="statement-heading"><strong>{{ statement.cycleStart | date:'dd/MM/yyyy' }} a {{ statement.cycleEnd | date:'dd/MM/yyyy' }}</strong><span class="statement-status">{{ statement.status === 'OPEN' ? 'Em andamento' : statement.status === 'CLOSED' ? 'Fechada' : statement.status === 'PAID' ? 'Paga' : statement.status === 'OVERDUE' ? 'Vencida' : statement.status }}</span></div><div class="statement-summary"><div class="statement-total"><span class="field-label">Total</span><strong>{{ formatMoney(statement.totalAmount) }}</strong></div><div class="statement-total"><span class="field-label">Pago</span><strong>{{ formatMoney(statement.paidAmount) }}</strong></div><div class="statement-total due"><span class="field-label">Devido</span><strong>{{ formatMoney(statement.outstandingAmount) }}</strong></div></div><div class="statement-meta"><span><strong>Fechamento:</strong> {{ statement.cycleEnd | date:'dd/MM/yyyy' }}</span><span><strong>Vencimento:</strong> {{ statement.dueDate | date:'dd/MM/yyyy' }}</span></div><div class="statement-items"><h3 class="statement-items-heading">Lançamentos ({{ statement.items.length }})</h3>@for (item of statement.items; track item.id) { <div class="statement-item"><div class="statement-item-info"><strong>{{ item.description }}</strong><span>Parcela {{ item.sequenceNumber }}/{{ item.totalInstallments }} · {{ item.cycleMonth | date:'dd/MM/yyyy' }}</span></div><strong class="statement-item-value">{{ formatMoney(item.amount) }}</strong></div> }</div> @if (statement.outstandingAmount > 0 && statement.status !== 'CANCELLED') { <button mat-flat-button color="primary" type="button" (click)="openPayment(statement)">Pagar fatura</button> }</section> } }</aside> }
      @if (paymentStatement()) { <button class="drawer-backdrop" type="button" aria-label="Fechar pagamento" (click)="closePayment()"></button><aside class="card-drawer payment-drawer" role="dialog" aria-modal="true"><header class="drawer-header"><h2>Pagar fatura</h2><button mat-icon-button type="button" aria-label="Fechar pagamento" (click)="closePayment()"><mat-icon>close</mat-icon></button></header><p>Valor devido: <strong>{{ formatMoney(paymentStatement()!.outstandingAmount) }}</strong></p><form class="card-form" [formGroup]="paymentForm" (ngSubmit)="payStatement()"><mat-form-field appearance="outline"><mat-label>Conta bancária</mat-label><mat-select formControlName="accountId">@for (account of bankAccounts(); track account.id) { <mat-option [value]="account.id">{{ account.name }}</mat-option> }</mat-select></mat-form-field><mat-form-field appearance="outline"><mat-label>Valor a pagar</mat-label><input matInput formControlName="amount" inputmode="decimal" /></mat-form-field><div class="drawer-actions"><button mat-button type="button" (click)="closePayment()">Cancelar</button><button mat-flat-button color="primary" type="submit" [disabled]="paymentForm.invalid || paymentSaving()">Confirmar pagamento</button></div></form></aside> }
      <mat-menu #cardMenu="matMenu"><ng-template matMenuContent let-card="card"><button mat-menu-item (click)="openEditForm(card)"><mat-icon>edit</mat-icon><span>Editar</span></button>@if (card.status === 'ACTIVE') { <button mat-menu-item (click)="deactivate(card)"><mat-icon>pause_circle</mat-icon><span>Desativar</span></button> } @else { <button mat-menu-item (click)="activate(card)"><mat-icon>play_circle</mat-icon><span>Reativar</span></button> }</ng-template></mat-menu>
    </main>
  `,
  styleUrl: './credit-cards.page.scss',
})
export class CreditCardsPageComponent implements OnInit {
  private readonly api = inject(CreditCardsApiService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly snackBar = inject(MatSnackBar); private readonly accountsApi = inject(AssetAccountsApiService);
  readonly cards = signal<CreditCard[]>([]); readonly selectedCard = signal<CreditCard | null>(null); readonly statements = signal<CreditCardStatement[]>([]); readonly statementLoading = signal(false); readonly paymentStatement = signal<CreditCardStatement | null>(null); readonly bankAccounts = signal<AssetAccount[]>([]); readonly paymentSaving = signal(false); readonly paymentForm = this.fb.group({ accountId: this.fb.control('', Validators.required), amount: this.fb.control('', [Validators.required, Validators.pattern(/^\d+(?:[,.]\d{1,2})?$/)]) }); readonly isLoading = signal(false); readonly loaded = signal(false); readonly loadError = signal<string | null>(null); readonly includeInactive = signal(false); readonly isFormOpen = signal(false); readonly editingCard = signal<CreditCard | null>(null); readonly isSaving = signal(false); readonly formError = signal<string | null>(null);
  readonly form: FormGroup<CreditCardForm> = this.fb.group({ name: this.fb.control('', [Validators.required, Validators.maxLength(100)]), institution: this.fb.control('', [Validators.required, Validators.maxLength(100)]), nickname: this.fb.control('', Validators.maxLength(100)), totalLimit: this.fb.control('', [Validators.required, Validators.pattern(/^\d+(?:[,.]\d{1,2})?$/)]), closingDay: this.fb.control(1, [Validators.required, Validators.min(1), Validators.max(31)]), dueDay: this.fb.control(1, [Validators.required, Validators.min(1), Validators.max(31)]), timezone: this.fb.control('America/Sao_Paulo', Validators.maxLength(64)) });
  readonly pageActions = [{ label: 'Novo cartão', icon: 'add', handler: () => this.openCreateForm() }]; readonly emptyActions = [{ label: 'Novo cartão', icon: 'add', handler: () => this.openCreateForm() }];
  ngOnInit(): void { this.loadCards(); this.accountsApi.list(false).subscribe({ next: a => this.bankAccounts.set(a.filter(x => x.type !== 'CASH')), error: () => undefined }); }
  openStatements(card: CreditCard): void { this.selectedCard.set(card); this.statementLoading.set(true); this.api.currentStatement(card.id).subscribe({ next: s => { this.statements.set([s]); this.statementLoading.set(false); }, error: () => this.statementLoading.set(false) }); }
  closeStatements(): void { this.selectedCard.set(null); this.statements.set([]); }
  openPayment(statement: CreditCardStatement): void { this.paymentStatement.set(statement); this.paymentForm.reset({ accountId: '', amount: statement.outstandingAmount.toFixed(2).replace('.', ',') }); }
  closePayment(): void { if (!this.paymentSaving()) this.paymentStatement.set(null); }
  payStatement(): void { const statement = this.paymentStatement(); if (!statement || this.paymentForm.invalid || this.paymentSaving()) return; const v = this.paymentForm.getRawValue(); if (!window.confirm('Confirmar pagamento da fatura?')) return; this.paymentSaving.set(true); const key = crypto.randomUUID(); this.api.payCurrent(statement.creditCardId, { bankAccountId: v.accountId, amount: Number(v.amount.replace(',', '.')), paidAt: new Date().toISOString() }, key).subscribe({ next: () => { this.paymentSaving.set(false); this.paymentStatement.set(null); this.statements.set([]); this.snackBar.open('Pagamento registrado.', 'Fechar', { duration: 3500 }); this.loadCards(); if (this.selectedCard()) { setTimeout(() => this.openStatements(this.selectedCard()!), 250); } }, error: () => { this.paymentSaving.set(false); this.snackBar.open('Não foi possível registrar o pagamento. Consulte a fatura antes de tentar novamente.', 'Fechar', { duration: 4500 }); } }); }
  loadCards(): void { this.isLoading.set(true); this.loadError.set(null); this.api.list(this.includeInactive()).subscribe({ next: cards => { this.cards.set(cards); this.loaded.set(true); this.isLoading.set(false); }, error: (error: HttpErrorResponse) => { this.isLoading.set(false); this.loadError.set(error.status === 403 ? 'Você não tem acesso aos cartões.' : 'Não foi possível carregar os cartões.'); } }); }
  setIncludeInactive(value: boolean): void { this.includeInactive.set(value); this.loadCards(); }
  formatMoney(value: number): string { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value); }
  openCreateForm(): void { this.editingCard.set(null); this.formError.set(null); this.form.reset({ name: '', institution: '', nickname: '', totalLimit: '', closingDay: 1, dueDay: 1, timezone: 'America/Sao_Paulo' }); this.isFormOpen.set(true); }
  openEditForm(card: CreditCard): void { this.editingCard.set(card); this.formError.set(null); this.form.reset({ name: card.name, institution: card.institution, nickname: card.nickname ?? '', totalLimit: card.totalLimit.toFixed(2).replace('.', ','), closingDay: card.closingDay, dueDay: card.dueDay, timezone: card.timezone }); this.isFormOpen.set(true); }
  closeForm(): void { if (!this.isSaving()) this.isFormOpen.set(false); }
  saveCard(): void { if (this.form.invalid || this.isSaving()) { this.form.markAllAsTouched(); return; } const values = this.form.getRawValue(); const request: CreditCardRequest = { name: values.name, institution: values.institution, nickname: values.nickname || null, totalLimit: Number(values.totalLimit.replace(',', '.')), closingDay: values.closingDay, dueDay: values.dueDay, timezone: values.timezone || 'America/Sao_Paulo' }; const current = this.editingCard(); this.isSaving.set(true); this.formError.set(null); (current ? this.api.update(current.id, request) : this.api.create(request)).subscribe({ next: () => { this.isSaving.set(false); this.isFormOpen.set(false); this.snackBar.open(current ? 'Cartão atualizado.' : 'Cartão cadastrado.', 'Fechar', { duration: 3500 }); this.loadCards(); }, error: (error: HttpErrorResponse) => { this.isSaving.set(false); this.formError.set(error.status === 409 ? 'O cartão foi alterado ou está em conflito. Atualize a lista e tente novamente.' : error.status === 404 ? 'Cartão não encontrado.' : 'Não foi possível concluir a operação.'); } }); }
  deactivate(card: CreditCard): void { if (!window.confirm(`Deseja desativar o cartão ${card.name}?`)) return; this.api.deactivate(card.id).subscribe({ next: () => { this.snackBar.open('Cartão desativado.', 'Fechar', { duration: 3000 }); this.loadCards(); }, error: () => this.snackBar.open('Não foi possível desativar o cartão.', 'Fechar', { duration: 3500 }) }); }
  activate(card: CreditCard): void { this.api.activate(card.id).subscribe({ next: () => { this.snackBar.open('Cartão reativado.', 'Fechar', { duration: 3000 }); this.loadCards(); }, error: () => this.snackBar.open('Não foi possível reativar o cartão.', 'Fechar', { duration: 3500 }) }); }
}
