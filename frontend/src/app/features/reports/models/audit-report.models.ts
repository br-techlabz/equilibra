export type AuditType = 'EXPENSE' | 'INCOME' | 'TRANSFER';
export type AuditStatus = 'ACTIVE' | 'CANCELLED';
export interface AuditTag { id: string; name: string; active: boolean; }
export interface AuditTransaction { id:string; type:AuditType; status:AuditStatus; description:string; occurredAt:string; createdAt:string; updatedAt:string; amount:number; categoryId:string; sourceAccountId:string|null; destinationAccountId:string|null; notes:string|null; tags:AuditTag[]; attachmentCount:number; }
export interface AuditReportResponse { content:AuditTransaction[]; page:number; size:number; totalElements:number; totalPages:number; }
export interface AuditReportFilters { from:string; to:string; accountIds:string[]; type:AuditType|'ALL'; status:AuditStatus|'ALL'; categoryId:string; tagIds:string[]; page:number; size:number; }
