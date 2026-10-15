import { Component, inject, signal, OnInit, DestroyRef } from '@angular/core';
import { FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { provideNativeDateAdapter } from '@angular/material/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { AssetAccountsApiService } from '../data-access/asset-accounts-api.service';
import {
  ReconcileAccountRequest,
  ReconciliationResultResponse,
} from '../models/reconciliation.models';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { ReconciliationResultDialogComponent } from './reconciliation-result-dialog.component';

interface ReconcileForm {
  reconciliationDate: FormControl<string>;
  informedBalance: FormControl<string>;
}

/* ============================================================
   Página de Reconciliação
   ============================================================ */

@Component({
  selector: 'app-reconcile-account-page',
  standalone: true,
  providers: [provideNativeDateAdapter()],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatButtonModule,
    MatDatepickerModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    ContentPanelComponent,
    PageHeaderComponent,
  ],
  template: `
    <main class="reconcile-page">
      <app-page-header
        title="Reconciliar Saldo"
        [subtitle]="accountName()"
        [actions]="backAction" />

      <app-content-panel>
        <form class="reconcile-form" [formGroup]="form" (ngSubmit)="onSubmit()">
          <!-- Data da reconciliação -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Data da reconciliação</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="reconciliationDate" [max]="maxDate" />
            <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
            @if (form.controls.reconciliationDate.hasError('required')) {
              <mat-error>Informe a data da reconciliação.</mat-error>
            }
            @if (form.controls.reconciliationDate.hasError('futureDate')) {
              <mat-error>Não é permitido reconciliar datas futuras.</mat-error>
            }
          </mat-form-field>

          <!-- Saldo de referência (preview) -->
          @if (referenceBalance() !== null) {
            <div class="reference-balance" role="status" aria-live="polite">
              <mat-icon aria-hidden="true">info_outline</mat-icon>
              <div class="reference-info">
                <span class="reference-label">Saldo calculado na data selecionada</span>
                <strong class="reference-value">{{ formatMoney(referenceBalance()!) }}</strong>
              </div>
            </div>
          }

          <!-- Saldo informado -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Saldo informado (extrato)</mat-label>
            <input matInput formControlName="informedBalance" inputmode="decimal" placeholder="0,00" />
            <mat-hint>Use até duas casas decimais. Ex.: 1.234,56 ou 1234.56</mat-hint>
            @if (form.controls.informedBalance.hasError('required')) {
              <mat-error>Informe o saldo.</mat-error>
            }
            @if (form.controls.informedBalance.hasError('pattern')) {
              <mat-error>Informe um valor monetário válido (até 2 casas decimais).</mat-error>
            }
          </mat-form-field>

          <!-- Aviso sobre ajuste -->
          <div class="reconcile-notice" role="note">
            <mat-icon aria-hidden="true">info</mat-icon>
            <p>
              Se houver diferença, o sistema criará uma <strong>receita</strong> (saldo informado maior)
              ou <strong>despesa</strong> (saldo informado menor) com descrição
              <em>"Ajuste de Saldo"</em> na categoria dedicada.
            </p>
          </div>

          <!-- Erro do formulário -->
          @if (formError()) {
            <p class="form-error" role="alert">
              <mat-icon aria-hidden="true">error_outline</mat-icon>{{ formError() }}
            </p>
          }

          <!-- Ações -->
          <div class="form-actions">
            <button mat-button type="button" routerLink="/accounts" [disabled]="isSubmitting()">
              Cancelar
            </button>
            <button
              mat-flat-button
              color="primary"
              type="submit"
              [disabled]="form.invalid || isSubmitting()"
              [attr.aria-busy]="isSubmitting()">
              @if (isSubmitting()) {
                <mat-spinner diameter="20" class="button-spinner"></mat-spinner>
                <span>Reconciliando...</span>
              } @else {
                <span>Confirmar Reconciliação</span>
              }
            </button>
          </div>
        </form>
      </app-content-panel>
    </main>
  `,
  styleUrl: './reconcile-account.page.scss',
})
export class ReconcileAccountPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly api = inject(AssetAccountsApiService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  readonly accountId = signal<string>('');
  readonly accountName = signal<string>('');
  readonly isSubmitting = signal(false);
  readonly formError = signal<string | null>(null);
  readonly referenceBalance = signal<number | null>(null);
  readonly isLoadingPreview = signal(false);
  readonly maxDate = new Date();

  readonly form: FormGroup<ReconcileForm> = this.formBuilder.group({
    reconciliationDate: this.formBuilder.control<string>('', [
      Validators.required,
      futureDateValidator(),
    ]),
    informedBalance: this.formBuilder.control<string>('', [
      Validators.required,
      moneyPatternValidator(),
    ]),
  });

  readonly backAction = [{
    label: 'Voltar',
    icon: 'arrow_back',
    handler: () => this.router.navigate(['/accounts']),
  }];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('accountId');
    if (!id) {
      this.router.navigate(['/accounts']);
      return;
    }
    this.accountId.set(id);
    this.loadAccountName();
    this.setupPreviewOnDateChange();
  }

  private loadAccountName(): void {
    this.api.list(false).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (accounts) => {
        const account = accounts.find((a) => a.id === this.accountId());
        if (account) {
          this.accountName.set(account.name);
        }
      },
    });
  }

  private setupPreviewOnDateChange(): void {
    this.form.controls.reconciliationDate.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((dateValue: string | Date | null) => {
        if (!dateValue) {
          this.referenceBalance.set(null);
          return;
        }
        const date = dateValue && typeof dateValue === 'object' && 'getTime' in dateValue
          ? dateValue as Date
          : new Date(String(dateValue) + 'T00:00:00');
        if (isNaN(date.getTime())) {
          this.referenceBalance.set(null);
          return;
        }
        const isoDate = date.toISOString().split('T')[0];
        this.fetchPreview(isoDate);
      });
  }

  private fetchPreview(date: string): void {
    this.isLoadingPreview.set(true);
    this.api.getBalanceAt(this.accountId(), date).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (res) => {
        this.referenceBalance.set(res.balance);
        this.isLoadingPreview.set(false);
      },
      error: () => {
        this.referenceBalance.set(null);
        this.isLoadingPreview.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    const values = this.form.getRawValue();
    const rawDate = values.reconciliationDate;
    const date = rawDate && typeof rawDate === 'object' && 'getTime' in rawDate
      ? rawDate as Date
      : new Date(String(rawDate) + 'T00:00:00');
    const isoDate = date.toISOString().split('T')[0];

    const request: ReconcileAccountRequest = {
      reconciliationDate: isoDate,
      informedBalance: parseMoneyInput(values.informedBalance),
    };

    this.isSubmitting.set(true);
    this.formError.set(null);

    this.api.reconcile(this.accountId(), request).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (result) => {
        this.isSubmitting.set(false);
        this.openResultDialog(result);
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        this.handleError(error);
      },
    });
  }

  private openResultDialog(result: ReconciliationResultResponse): void {
    const dialogRef = this.dialog.open(ReconciliationResultDialogComponent, {
      data: { result },
      disableClose: true,
      panelClass: 'reconciliation-result-dialog',
      minWidth: '320px',
      maxWidth: '480px',
    });

    dialogRef.afterClosed().subscribe(() => {
      this.router.navigate(['/accounts']);
    });
  }

  private handleError(error: HttpErrorResponse): void {
    let message = 'Não foi possível concluir a reconciliação.';
    if (error.error?.detail) {
      message = error.error.detail;
    } else if (error.status === 400) {
      message = 'Dados inválidos. Verifique a data e o valor informado.';
    } else if (error.status === 404) {
      message = 'Conta não encontrada.';
    } else if (error.status === 409) {
      message = 'Conta inativa.';
    } else if (error.status === 401 || error.status === 403) {
      message = 'Sessão expirada. Faça login novamente.';
    }
    this.formError.set(message);
  }

  formatMoney(value: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
  }
}

/* ============================================================
   Validadores Customizados
   ============================================================ */

function futureDateValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const date = new Date(control.value + 'T00:00:00');
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return date > today ? { futureDate: true } : null;
  };
}

function moneyPatternValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const normalized = control.value.replace(',', '.');
    const num = Number(normalized);
    if (isNaN(num)) return { pattern: true };
    const parts = normalized.split('.');
    if (parts[1] && parts[1].length > 2) return { pattern: true };
    return null;
  };
}

function parseMoneyInput(value: string): number {
  return Number(value.replace(',', '.'));
}