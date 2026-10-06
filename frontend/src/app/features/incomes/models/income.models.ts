export type IncomeStatus = 'ACTIVE' | 'CANCELLED';
export interface Income { id:string; description:string; occurredAt:string; accountId:string; amount:number; categoryId:string; notes:string|null; status:IncomeStatus; createdAt:string; updatedAt:string; }
export interface IncomeRequest { description:string; occurredAt:string; accountId:string; amount:number; categoryId:string; notes?:string|null; }
export interface IncomePage { content: Income[]; page:number; size:number; totalElements:number; totalPages:number; }
