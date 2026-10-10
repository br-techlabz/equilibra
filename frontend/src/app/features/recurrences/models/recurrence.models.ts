export type RecurrenceType = 'EXPENSE' | 'INCOME';
export type RecurrenceFrequency = 'MONTHLY' | 'YEARLY';
export type RecurrenceStatus = 'ACTIVE' | 'PAUSED' | 'ENDED' | 'CANCELLED';

export interface Recurrence {
  id: string;
  type: RecurrenceType;
  description: string;
  amount: number | string;
  categoryId: string;
  accountId: string;
  frequency: RecurrenceFrequency;
  startDate: string;
  endDate: string | null;
  dueDay: number;
  status: RecurrenceStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRecurrenceRequest {
  type: RecurrenceType;
  description: string;
  amount: number;
  categoryId: string;
  accountId: string;
  frequency: RecurrenceFrequency;
  startDate: string;
  endDate: string | null;
  dueDay: number;
}

export interface UpdateRecurrenceRequest {
  description: string;
  amount: number;
  endDate: string | null;
  dueDay: number;
}

export const RECURRENCE_TYPES: ReadonlyArray<{ value: RecurrenceType; label: string }> = [
  { value: 'EXPENSE', label: 'Despesa' },
  { value: 'INCOME', label: 'Receita' },
];

export const RECURRENCE_FREQUENCIES: ReadonlyArray<{ value: RecurrenceFrequency; label: string }> = [
  { value: 'MONTHLY', label: 'Mensal' },
  { value: 'YEARLY', label: 'Anual' },
];
