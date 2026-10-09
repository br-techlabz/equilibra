export interface Budget { id:string; categoryId:string; categoryName:string; categoryActive:boolean; year:number; month:number; plannedAmount:number|string; createdAt:string; updatedAt:string; }
export interface CreateBudgetRequest { categoryId:string; month:string; plannedAmount:number; }
export interface UpdateBudgetRequest { plannedAmount:number; }
