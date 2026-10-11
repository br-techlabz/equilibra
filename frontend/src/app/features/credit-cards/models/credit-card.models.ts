export type CreditCardStatus = 'ACTIVE' | 'INACTIVE';

export interface CreditCard {
  id: string;
  name: string;
  institution: string;
  nickname: string | null;
  totalLimit: number;
  committedLimit: number;
  reservedLimit: number;
  availableLimit: number;
  closingDay: number;
  dueDay: number;
  timezone: string;
  status: CreditCardStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreditCardStatementItem { id: string; purchaseId: string; sequenceNumber: number; totalInstallments: number; amount: number; cycleMonth: string; }
export interface CreditCardStatement { id: string; creditCardId: string; cycleStart: string; cycleEnd: string; closingAt: string; dueDate: string; status: 'OPEN' | 'CLOSED' | 'OVERDUE' | 'PARTIALLY_PAID' | 'PAID' | 'CANCELLED'; totalAmount: number; paidAmount: number; outstandingAmount: number; items: CreditCardStatementItem[]; }
export interface StatementPaymentRequest { bankAccountId: string; amount: number; paidAt: string; }

export interface CreditCardRequest {
  name: string;
  institution: string;
  nickname?: string | null;
  totalLimit: number;
  closingDay: number;
  dueDay: number;
  timezone?: string | null;
}
