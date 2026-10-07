export interface FinancialReportPeriod {
  from: string;
  to: string;
}

export interface FinancialReportAccount {
  id: string;
  name: string;
  active: boolean;
}

export type FinancialReportType = 'EXPENSE' | 'INCOME' | 'TRANSFER';
export type FinancialReportStatus = 'ACTIVE' | 'CANCELLED';

export interface FinancialReportTransaction {
  id: string;
  type: FinancialReportType;
  status: FinancialReportStatus;
  description: string;
  occurredAt: string;
  amount: number;
  categoryId: string;
  sourceAccountId: string | null;
  destinationAccountId: string | null;
  notes: string | null;
}

export interface FinancialReportDetails {
  content: FinancialReportTransaction[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface FinancialReportResponse {
  period: FinancialReportPeriod;
  accounts: FinancialReportAccount[];
  openingBalance: number;
  incomeTotal: number;
  expenseTotal: number;
  financialResult: number;
  incomingTransferTotal: number;
  outgoingTransferTotal: number;
  internalTransferTotal: number;
  balanceChange: number;
  closingBalance: number;
  details: FinancialReportDetails;
}

export interface FinancialReportFilters {
  from: string;
  to: string;
  accountIds: string[];
  page: number;
  size: number;
}
