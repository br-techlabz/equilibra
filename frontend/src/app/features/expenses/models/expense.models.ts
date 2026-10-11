export type ExpenseStatus = 'ACTIVE' | 'CANCELLED';

export interface Expense {
  id: string;
  description: string;
  occurredAt: string;
  accountId: string | null;
  amount: number;
  paymentMethod?: 'BANK_ACCOUNT' | 'CREDIT_CARD';
  creditCardId?: string | null;
  installmentCount?: number | null;
  cycleMonth?: string | null;
  purchaseAt?: string | null;
  categoryId: string;
  notes: string | null;
  tags?: { id: string; name: string; active: boolean }[];
  status: ExpenseStatus;
  createdAt: string;
  updatedAt: string;
}

export type ExpensePaymentMethod = 'BANK_ACCOUNT' | 'CASH' | 'CREDIT_CARD';

export interface ExpenseRequest {
  description: string;
  occurredAt: string;
  accountId: string | null;
  amount: number;
  categoryId: string;
  notes?: string | null;
  tagIds?: string[];
}

export interface CreditCardPurchaseRequest {
  description: string;
  purchaseDate: string;
  purchaseAt?: string;
  totalAmount: number;
  installmentCount: number;
  creditCardId: string;
  categoryId: string;
}

export interface CreditCardPurchase {
  id: string;
  creditCardId: string;
  categoryId: string;
  description: string;
  totalAmount: number;
  purchaseDate: string;
  installmentCount: number;
  cycleMonth: string;
  status: 'ACTIVE' | 'REVERSED' | 'CANCELLED';
  installments: Array<{ sequenceNumber: number; totalInstallments: number; principalAmount: number; cycleMonth: string }>;
}

export interface ExpensePage {
  content: Expense[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
