import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ReconciliationResultResponse } from '../models/reconciliation.models';

interface ReconciliationResultDialogData {
  result: ReconciliationResultResponse;
}

@Component({
  selector: 'app-reconciliation-result-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title class="dialog-title">
      <mat-icon [class]="getIconClass()">
        {{ getIcon() }}
      </mat-icon>
      {{ result.adjustmentType === 'NONE' ? 'Saldo já reconciliado' : 'Reconciliação concluída' }}
    </h2>

    <mat-dialog-content class="dialog-content">
      <div class="result-grid">
        <div class="result-item">
          <span class="result-label">Data reconciliada</span>
          <strong class="result-value">{{ formatDate(result.reconciliationDate) }}</strong>
        </div>
        <div class="result-item">
          <span class="result-label">Saldo calculado</span>
          <strong class="result-value">{{ formatMoney(result.calculatedBalance) }}</strong>
        </div>
        <div class="result-item">
          <span class="result-label">Saldo informado</span>
          <strong class="result-value">{{ formatMoney(result.informedBalance) }}</strong>
        </div>
        <div class="result-item">
          <span class="result-label">Valor ajustado</span>
          <strong class="result-value" [class]="getDifferenceClass()">
            {{ formatMoney(result.difference) }}
          </strong>
        </div>
        @if (result.adjustmentType !== 'NONE') {
          <div class="result-item">
            <span class="result-label">Lançamento criado</span>
            <strong class="result-value">
              {{ result.adjustmentType === 'INCOME' ? 'Receita' : 'Despesa' }} — "Ajuste de Saldo"
            </strong>
          </div>
          <div class="result-item">
            <span class="result-label">ID do lançamento</span>
            <code class="result-value transaction-id">{{ result.createdTransactionId }}</code>
          </div>
        } @else {
          <div class="result-item">
            <span class="result-label">Resultado</span>
            <strong class="result-value" style="color: var(--color-success);">Sem diferença — nenhum lançamento criado</strong>
          </div>
        }
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="dialog-actions">
      <button mat-flat-button color="primary" [mat-dialog-close]="true">
        Fechar e voltar às contas
      </button>
    </mat-dialog-actions>
  `,
  styleUrl: './reconciliation-result-dialog.component.scss',
})
export class ReconciliationResultDialogComponent {
  readonly result: ReconciliationResultResponse;

  constructor(
    private readonly dialogRef: MatDialogRef<ReconciliationResultDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data: ReconciliationResultDialogData,
  ) {
    this.result = data.result;
  }

  getIcon(): string {
    if (this.result.adjustmentType === 'INCOME') return 'trending_up';
    if (this.result.adjustmentType === 'EXPENSE') return 'trending_down';
    return 'check_circle';
  }

  getIconClass(): string {
    if (this.result.adjustmentType === 'INCOME') return 'icon-income';
    if (this.result.adjustmentType === 'EXPENSE') return 'icon-expense';
    return 'icon-none';
  }

  getDifferenceClass(): string {
    if (this.result.difference > 0) return 'positive';
    if (this.result.difference < 0) return 'negative';
    return '';
  }

  formatMoney(value: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
  }

  formatDate(value: string): string {
    const date = new Date(value + 'T00:00:00');
    return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(date);
  }
}