import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatMenuModule } from '@angular/material/menu';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { AssetAccountsApiService } from '../data-access/asset-accounts-api.service';
import {
  ASSET_ACCOUNT_TYPE_OPTIONS,
  AssetAccount,
  AssetAccountRequest,
  AssetAccountType,
  assetAccountTypeOption,
} from '../models/asset-account.models';

interface AssetAccountForm {
  name: FormControl<string>;
  type: FormControl<AssetAccountType>;
  initialBalance: FormControl<string>;
}

@Component({
  selector: 'app-asset-accounts-page',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatMenuModule,
    PageHeaderComponent,
    ContentPanelComponent,
    EmptyStateComponent,
  ],
  template: `
    <div class="accounts-page">
      <app-page-header
        title="Contas"
        subtitle="Gerencie onde seu dinheiro está."
        [actions]="headerActions" />

      <app-content-panel
        title="Contas de ativo"
        subtitle="Contas utilizadas para acompanhar seus recursos financeiros.">
        <div class="list-toolbar">
          <mat-checkbox [checked]="includeInactive()" (change)="toggleInactive($event.checked)">
            Mostrar contas inativas
          </mat-checkbox>
        </div>

        @if (isLoading()) {
          <div class="state-block" role="status" aria-live="polite">
            <mat-spinner diameter="36" />
            <span>Carregando contas...</span>
          </div>
        } @else if (errorMessage()) {
          <div class="state-block error-block" role="alert">
            <mat-icon>error_outline</mat-icon>
            <p>{{ errorMessage() }}</p>
            <button mat-stroked-button type="button" (click)="loadAccounts()">Tentar novamente</button>
          </div>
        } @else if (accounts().length === 0) {
          <app-empty-state
            icon="account_balance_wallet"
            iconVariant="folder"
            title="Nenhuma conta cadastrada"
            description="Cadastre sua primeira conta para começar a organizar seus recursos financeiros."
            [centered]="false"
            [actions]="[{ label: 'Nova conta', icon: 'add', handler: openCreateForm }]" />
        } @else {
          <div class="desktop-table-wrapper">
            <table class="accounts-table">
              <thead>
                <tr>
                  <th scope="col">Conta</th>
                  <th scope="col">Tipo</th>
                  <th scope="col">Saldo inicial</th>
                  <th scope="col">Status</th>
                  <th scope="col">Atualizada em</th>
                  <th scope="col"><span class="sr-only">Ações</span></th>
                </tr>
              </thead>
              <tbody>
                @for (account of accounts(); track account.id) {
                  <tr>
                    <td><span class="account-name"><mat-icon aria-hidden="true">{{ typeOption(account.type).icon }}</mat-icon>{{ account.name }}</span></td>
                    <td>{{ typeOption(account.type).label }}</td>
                    <td>{{ account.initialBalance | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
                    <td><span class="status-badge" [class.inactive]="!account.active">{{ account.active ? 'Ativa' : 'Inativa' }}</span></td>
                    <td>{{ account.updatedAt | date:'dd/MM/yyyy HH:mm':'':'pt-BR' }}</td>
                    <td class="actions-cell">
                      <button mat-icon-button [matMenuTriggerFor]="actionsMenu" [matMenuTriggerData]="{ account }" [attr.aria-label]="'Ações da conta ' + account.name">
                        <mat-icon>more_vert</mat-icon>
                      </button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <div class="mobile-account-list">
            @for (account of accounts(); track account.id) {
              <article class="account-card">
                <div class="account-card-header">
                  <span class="account-name"><mat-icon aria-hidden="true">{{ typeOption(account.type).icon }}</mat-icon>{{ account.name }}</span>
                  <button mat-icon-button [matMenuTriggerFor]="actionsMenu" [matMenuTriggerData]="{ account }" [attr.aria-label]="'Ações da conta ' + account.name"><mat-icon>more_vert</mat-icon></button>
                </div>
                <span class="account-type">{{ typeOption(account.type).label }}</span>
                <span class="account-label">Saldo inicial</span>
                <strong class="account-balance">{{ account.initialBalance | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong>
                <div class="account-card-footer">
                  <span class="status-badge" [class.inactive]="!account.active">{{ account.active ? 'Ativa' : 'Inativa' }}</span>
                  <span>Atualizada em {{ account.updatedAt | date:'dd/MM/yyyy':'':'pt-BR' }}</span>
                </div>
              </article>
            }
          </div>
        }
      </app-content-panel>

      @if (isFormOpen()) {
        <button class="form-backdrop" type="button" aria-label="Fechar formulário" (click)="closeForm()"></button>
        <aside class="account-form-drawer" role="dialog" aria-modal="true" aria-labelledby="account-form-title">
          <div class="drawer-header">
            <div>
              <span class="panel-kicker">Contas</span>
              <h2 id="account-form-title">{{ editingAccount() ? 'Editar conta' : 'Nova conta' }}</h2>
            </div>
            <button mat-icon-button type="button" (click)="closeForm()" aria-label="Fechar formulário"><mat-icon>close</mat-icon></button>
          </div>
          <form [formGroup]="form" (ngSubmit)="saveAccount()" class="account-form" novalidate>
            <mat-form-field appearance="outline">
              <mat-label>Nome da conta</mat-label>
              <input matInput formControlName="name" placeholder="Ex.: Conta corrente" />
              @if (form.controls.name.hasError('required')) { <mat-error>Nome é obrigatório.</mat-error> }
              @if (form.controls.name.hasError('maxlength')) { <mat-error>Use no máximo 100 caracteres.</mat-error> }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Tipo de conta</mat-label>
              <mat-select formControlName="type">
                @for (option of typeOptions; track option.value) { <mat-option [value]="option.value">{{ option.label }}</mat-option> }
              </mat-select>
              @if (form.controls.type.hasError('required')) { <mat-error>Tipo é obrigatório.</mat-error> }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Saldo inicial</mat-label>
              <input matInput inputmode="decimal" formControlName="initialBalance" placeholder="0,00" aria-describedby="balance-help" />
              <mat-hint id="balance-help">Informe o saldo no momento do cadastro.</mat-hint>
              @if (form.controls.initialBalance.hasError('required')) { <mat-error>Saldo inicial é obrigatório.</mat-error> }
              @if (form.controls.initialBalance.hasError('pattern')) { <mat-error>Informe um valor com até duas casas decimais.</mat-error> }
            </mat-form-field>
            @if (formError()) { <div class="form-error" role="alert"><mat-icon>error_outline</mat-icon>{{ formError() }}</div> }
            <div class="drawer-actions">
              <button mat-button type="button" (click)="closeForm()">Cancelar</button>
              <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || isSaving()">
                @if (isSaving()) { <mat-spinner diameter="18" /> } @else { {{ editingAccount() ? 'Salvar alterações' : 'Salvar conta' }} }
              </button>
            </div>
          </form>
        </aside>
      }

      <mat-menu #actionsMenu="matMenu">
        <ng-template matMenuContent let-account="account">
          <button mat-menu-item (click)="openEditForm(account)"><mat-icon>edit</mat-icon><span>Editar</span></button>
          @if (account.active) { <button mat-menu-item (click)="deactivate(account)"><mat-icon>pause_circle</mat-icon><span>Desativar</span></button> }
          @else { <button mat-menu-item (click)="activate(account)"><mat-icon>play_circle</mat-icon><span>Reativar</span></button> }
        </ng-template>
      </mat-menu>
    </div>
  `,
  styleUrl: './asset-accounts.page.scss',
})
export class AssetAccountsPageComponent {
  private readonly api = inject(AssetAccountsApiService);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly accounts = signal<AssetAccount[]>([]);
  readonly includeInactive = signal(false);
  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly isFormOpen = signal(false);
  readonly editingAccount = signal<AssetAccount | null>(null);
  readonly typeOptions = ASSET_ACCOUNT_TYPE_OPTIONS;

  readonly form: FormGroup<AssetAccountForm> = this.formBuilder.group({
    name: this.formBuilder.control('', [Validators.required, Validators.maxLength(100)]),
    type: this.formBuilder.control<AssetAccountType>('CHECKING', Validators.required),
    initialBalance: this.formBuilder.control('0,00', [Validators.required, Validators.pattern(/^-?\d+(?:[,.]\d{1,2})?$/)]),
  });

  readonly headerActions = [{ label: 'Nova conta', icon: 'add', handler: () => this.openCreateForm() }];

  constructor() { this.loadAccounts(); }

  loadAccounts(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.api.list(this.includeInactive()).subscribe({
      next: (accounts) => { this.accounts.set(accounts); this.isLoading.set(false); },
      error: () => { this.errorMessage.set('Não foi possível carregar suas contas.'); this.isLoading.set(false); },
    });
  }

  toggleInactive(value: boolean): void { this.includeInactive.set(value); this.loadAccounts(); }
  typeOption(type: AssetAccountType) { return assetAccountTypeOption(type); }

  openCreateForm(): void {
    this.editingAccount.set(null); this.formError.set(null); this.form.reset({ name: '', type: 'CHECKING', initialBalance: '0,00' }); this.isFormOpen.set(true);
  }

  openEditForm(account: AssetAccount): void {
    this.editingAccount.set(account); this.formError.set(null); this.form.reset({ name: account.name, type: account.type, initialBalance: account.initialBalance.toFixed(2).replace('.', ',') }); this.isFormOpen.set(true);
  }

  closeForm(): void { if (!this.isSaving()) this.isFormOpen.set(false); }

  saveAccount(): void {
    if (this.form.invalid || this.isSaving()) { this.form.markAllAsTouched(); return; }
    const raw = this.form.getRawValue();
    const initialBalance = Number(raw.initialBalance.replace(',', '.'));
    const request: AssetAccountRequest = { name: raw.name, type: raw.type, initialBalance };
    this.isSaving.set(true); this.formError.set(null);
    const account = this.editingAccount();
    const operation = account ? this.api.update(account.id, request) : this.api.create(request);
    operation.subscribe({
      next: () => { this.isSaving.set(false); this.isFormOpen.set(false); this.snackBar.open(account ? 'Conta atualizada com sucesso.' : 'Conta criada com sucesso.', 'Fechar', { duration: 3500 }); this.loadAccounts(); },
      error: (error: HttpErrorResponse) => { this.isSaving.set(false); this.formError.set(error.status === 409 ? 'Já existe uma conta ativa com esse nome.' : 'Não foi possível concluir a operação. Tente novamente.'); },
    });
  }

  deactivate(account: AssetAccount): void {
    if (!window.confirm('A conta deixará de estar disponível para novos lançamentos, mas permanecerá no seu histórico.')) return;
    this.api.deactivate(account.id).subscribe({ next: () => { this.snackBar.open('Conta desativada.', 'Fechar', { duration: 3000 }); this.loadAccounts(); }, error: () => this.snackBar.open('Não foi possível desativar a conta.', 'Fechar', { duration: 3500 }) });
  }

  activate(account: AssetAccount): void {
    this.api.activate(account.id).subscribe({ next: () => { this.snackBar.open('Conta reativada.', 'Fechar', { duration: 3000 }); this.loadAccounts(); }, error: (error: HttpErrorResponse) => this.snackBar.open(error.status === 409 ? 'Não é possível reativar: já existe uma conta ativa com o mesmo nome.' : 'Não foi possível reativar a conta.', 'Fechar', { duration: 4000 }) });
  }
}
