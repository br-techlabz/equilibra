export interface ReconcileAccountRequest {
  reconciliationDate: string;
  informedBalance: number;
}

export interface ReconciliationResultResponse {
  accountId: string;
  reconciliationDate: string;
  calculatedBalance: number;
  informedBalance: number;
  difference: number;
  adjustmentType: 'INCOME' | 'EXPENSE' | 'NONE';
  createdTransactionId: string | null;
}

export interface ReconciliationPreviewResponse {
  balance: number;
}