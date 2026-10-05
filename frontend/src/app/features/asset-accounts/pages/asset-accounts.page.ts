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
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AssetAccountsApiService } from '../data-access/asset-accounts-api.service';
import {
  ASSET_ACCOUNT_TYPE_OPTIONS,
  AssetAccount,
  AssetAccountRequest,
  AssetAccountType,
  assetAccountTypeOption,
} from '../models/asset-account.models';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';

interface AccountForm {
  name: FormControl<string>;
  type: FormControl<AssetAccountType>;
  initialBalance: FormControl<string>;
}

@Component({
  selector: 'app-asset-accounts-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatMenuModule,
    MatSelectModule,
    MatSnackBarModule,
    ContentPanelComponent,
    EmptyStateComponent,
    PageHeaderComponent,
  ],
  template: `
    <main class="accounts-page">
      <app-page-header
        title="Contas de ativo"
        subtitle="Acompanhe as contas onde seus recursos estão disponíveis."
        [actions]="pageActions" />

      <app-content-panel title="Minhas contas" subtitle="Contas correntes, poupanças, dinheiro e investimentos.">
        <div class="accounts-toolbar">
          <mat-checkbox [checked]="includeInactive()" (change)="setIncludeInactive($event.checked)">
            Mostrar contas inativas
          </mat-checkbox>
          <span class="account-count">{{ accounts().length }} {{ accounts().length === 1 ? 'conta' : 'contas' }}</span>
        </div>

        @if (accountsLoaded() && accounts().length === 0) {
          <app-empty-state
            icon="account_balance_wallet"
            iconVariant="folder"
            title="Nenhuma conta cadastrada"
            description="Cadastre sua primeira conta de ativo para começar."
            [centered]="false"
            [actions]="emptyActions" />
        } @else if (accounts().length > 0) {
          <div class="account-list" role="list">
            @for (account of accounts(); track account.id) {
              <article class="account-row" role="listitem">
                <div class="account-identity">
                  <span class="account-icon" aria-hidden="true">
                    <mat-icon>{{ typeOption(account.type).icon }}</mat-icon>
                  </span>
                  <div class="account-title">
                    <strong>{{ account.name }}</strong>
                    <span>{{ typeOption(account.type).label }}</span>
                  </div>
                </div>

                <div class="account-value">
                  <span class="field-label">Saldo inicial</span>
                  <strong>{{ formatMoney(account.initialBalance) }}</strong>
                </div>

                <div class="account-status">
                  <span class="field-label">Status</span>
                  <span class="status" [class.status-inactive]="!account.active">
                    <span class="status-dot" aria-hidden="true"></span>
                    {{ account.active ? 'Ativa' : 'Inativa' }}
                  </span>
                </div>

                <div class="account-updated">
                  <span class="field-label">Atualizada em</span>
                  <span>{{ formatDate(account.updatedAt) }}</span>
                </div>

                <button
                  mat-icon-button
                  class="account-actions"
                  [matMenuTriggerFor]="accountMenu"
                  [matMenuTriggerData]="{ account: account }"
                  [attr.aria-label]="'Ações da conta ' + account.name">
                  <mat-icon>more_vert</mat-icon>
                </button>
              </article>
            }
          </div>
        }
      </app-content-panel>

      @if (isFormOpen()) {
        <button class="drawer-backdrop" type="button" aria-label="Fechar formulário" (click)="closeForm()"></button>
        <aside class="account-drawer" role="dialog" aria-modal="true" aria-labelledby="drawer-title">
          <header class="drawer-header">
            <div>
              <span class="eyebrow">Contas de ativo</span>
              <h2 id="drawer-title">{{ editingAccount() ? 'Editar conta' : 'Nova conta' }}</h2>
            </div>
            <button mat-icon-button type="button" aria-label="Fechar formulário" (click)="closeForm()">
              <mat-icon>close</mat-icon>
            </button>
          </header>

          <form class="account-form" [formGroup]="form" (ngSubmit)="saveAccount()" novalidate>
            <mat-form-field appearance="outline">
              <mat-label>Nome da conta</mat-label>
              <input matInput formControlName="name" placeholder="Ex.: Conta corrente" />
              @if (form.controls.name.hasError('required')) { <mat-error>Informe o nome da conta.</mat-error> }
              @if (form.controls.name.hasError('maxlength')) { <mat-error>Use no máximo 100 caracteres.</mat-error> }
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Tipo de conta</mat-label>
              <mat-select formControlName="type">
                @for (option of typeOptions; track option.value) {
                  <mat-option [value]="option.value">{{ option.label }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Saldo inicial</mat-label>
              <input matInput formControlName="initialBalance" inputmode="decimal" placeholder="0,00" />
              <mat-hint>Use até duas casas decimais.</mat-hint>
              @if (form.controls.initialBalance.hasError('required')) { <mat-error>Informe o saldo inicial.</mat-error> }
              @if (form.controls.initialBalance.hasError('pattern')) { <mat-error>Informe um valor válido.</mat-error> }
            </mat-form-field>

            @if (formError()) {
              <p class="form-error" role="alert"><mat-icon aria-hidden="true">error_outline</mat-icon>{{ formError() }}</p>
            }

            <div class="drawer-actions">
              <button mat-button type="button" (click)="closeForm()">Cancelar</button>
              <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || isSaving()">
                {{ editingAccount() ? 'Salvar alterações' : 'Cadastrar conta' }}
              </button>
            </div>
          </form>
        </aside>
      }

      <mat-menu #accountMenu="matMenu">
        <ng-template matMenuContent let-account="account">
          <button mat-menu-item (click)="openEditForm(account)">
            <mat-icon>edit</mat-icon><span>Editar</span>
          </button>
          @if (account.active) {
            <button mat-menu-item (click)="deactivate(account)">
              <mat-icon>pause_circle</mat-icon><span>Desativar</span>
            </button>
          } @else {
            <button mat-menu-item (click)="activate(account)">
              <mat-icon>play_circle</mat-icon><span>Reativar</span>
            </button>
          }
        </ng-template>
      </mat-menu>
    </main>
  `,
  styleUrl: './asset-accounts.page.scss',
})
export class AssetAccountsPageComponent implements OnInit {
  private readonly api = inject(AssetAccountsApiService);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly accounts = signal<AssetAccount[]>([]);
  readonly accountsLoaded = signal(false);
  readonly includeInactive = signal(false);
  readonly isFormOpen = signal(false);
  readonly editingAccount = signal<AssetAccount | null>(null);
  readonly isSaving = signal(false);
  readonly formError = signal<string | null>(null);
  readonly typeOptions = ASSET_ACCOUNT_TYPE_OPTIONS;

  readonly form: FormGroup<AccountForm> = this.formBuilder.group({
    name: this.formBuilder.control('', [Validators.required, Validators.maxLength(100)]),
    type: this.formBuilder.control<AssetAccountType>('CHECKING', Validators.required),
    initialBalance: this.formBuilder.control('0,00', [
      Validators.required,
      Validators.pattern(/^-?\d+(?:[,.]\d{1,2})?$/),
    ]),
  });

  readonly pageActions = [{ label: 'Nova conta', icon: 'add', handler: () => this.openCreateForm() }];
  readonly emptyActions = [{ label: 'Nova conta', icon: 'add', handler: () => this.openCreateForm() }];

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.accountsLoaded.set(false);
    this.api.list(this.includeInactive()).subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);
        this.accountsLoaded.set(true);
      },
      error: () => {
        this.accounts.set([]);
        this.snackBar.open('Não foi possível carregar as contas.', 'Fechar', { duration: 4000 });
      },
    });
  }

  setIncludeInactive(value: boolean): void {
    this.includeInactive.set(value);
    this.loadAccounts();
  }

  typeOption(type: AssetAccountType) {
    return assetAccountTypeOption(type);
  }

  formatMoney(value: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
  }

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '—' : new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(date);
  }

  openCreateForm(): void {
    this.editingAccount.set(null);
    this.formError.set(null);
    this.form.reset({ name: '', type: 'CHECKING', initialBalance: '0,00' });
    this.isFormOpen.set(true);
  }

  openEditForm(account: AssetAccount): void {
    this.editingAccount.set(account);
    this.formError.set(null);
    this.form.reset({
      name: account.name,
      type: account.type,
      initialBalance: account.initialBalance.toFixed(2).replace('.', ','),
    });
    this.isFormOpen.set(true);
  }

  closeForm(): void {
    if (!this.isSaving()) this.isFormOpen.set(false);
  }

  saveAccount(): void {
    if (this.form.invalid || this.isSaving()) {
      this.form.markAllAsTouched();
      return;
    }

    const values = this.form.getRawValue();
    const request: AssetAccountRequest = {
      name: values.name,
      type: values.type,
      initialBalance: Number(values.initialBalance.replace(',', '.')),
    };
    const current = this.editingAccount();
    const operation = current ? this.api.update(current.id, request) : this.api.create(request);

    this.isSaving.set(true);
    this.formError.set(null);
    operation.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isFormOpen.set(false);
        this.snackBar.open(current ? 'Conta atualizada.' : 'Conta cadastrada.', 'Fechar', { duration: 3500 });
        this.loadAccounts();
      },
      error: (error: HttpErrorResponse) => {
        this.isSaving.set(false);
        this.formError.set(error.status === 409 ? 'Já existe uma conta ativa com esse nome.' : 'Não foi possível concluir a operação.');
      },
    });
  }

  deactivate(account: AssetAccount): void {
    if (!window.confirm('Deseja desativar esta conta?')) return;
    this.api.deactivate(account.id).subscribe({
      next: () => {
        this.snackBar.open('Conta desativada.', 'Fechar', { duration: 3000 });
        this.loadAccounts();
      },
      error: () => this.snackBar.open('Não foi possível desativar a conta.', 'Fechar', { duration: 3500 }),
    });
  }

  activate(account: AssetAccount): void {
    this.api.activate(account.id).subscribe({
      next: () => {
        this.snackBar.open('Conta reativada.', 'Fechar', { duration: 3000 });
        this.loadAccounts();
      },
      error: (error: HttpErrorResponse) => this.snackBar.open(
        error.status === 409 ? 'Já existe uma conta ativa com esse nome.' : 'Não foi possível reativar a conta.',
        'Fechar',
        { duration: 4000 },
      ),
    });
  }
}
