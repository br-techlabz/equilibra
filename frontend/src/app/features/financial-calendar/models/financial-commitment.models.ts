export type CommitmentType = 'EXPENSE' | 'INCOME';
export type CommitmentStatus = 'PENDING' | 'SETTLED' | 'CANCELLED';
export interface FinancialCommitment { id:string; type:CommitmentType; description:string; plannedAmount:number|string; dueDate:string; accountId:string; categoryId:string; recurrenceRuleId:string|null; status:CommitmentStatus; financialTransactionId:string|null; settledAt:string|null; overdue:boolean; createdAt:string; updatedAt:string; }
export interface CommitmentPage { content:FinancialCommitment[]; totalElements:number; totalPages:number; number:number; size:number; }
export interface CommitmentFilters { type?:CommitmentType; status?:CommitmentStatus; from:string; to:string; page?:number; size?:number; }
export interface CreateCommitmentRequest { type:CommitmentType; description:string; plannedAmount:number; dueDate:string; accountId:string; categoryId:string; recurrenceRuleId?:string|null; }
export interface UpdateCommitmentRequest { description:string; plannedAmount:number; dueDate:string; accountId:string; categoryId:string; }
export interface SettleCommitmentRequest { occurredAt:string; amount:number; }
