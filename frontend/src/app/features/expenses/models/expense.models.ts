export type ExpenseStatus = 'ACTIVE' | 'CANCELLED';

export interface Expense {
  id: string;
  description: string;
  occurredAt: string;
  accountId: string;
  amount: number;
  categoryId: string;
  notes: string | null;
  status: ExpenseStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ExpenseRequest {
  description: string;
  occurredAt: string;
  accountId: string;
  amount: number;
  categoryId: string;
  notes?: string | null;
}

export interface ExpensePage {
  content: Expense[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
