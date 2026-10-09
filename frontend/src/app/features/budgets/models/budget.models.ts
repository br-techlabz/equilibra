export interface Budget { id:string; categoryId:string; categoryName:string; categoryActive:boolean; year:number; month:number; plannedAmount:number|string; createdAt:string; updatedAt:string; }
export interface CreateBudgetRequest { categoryId:string; month:string; plannedAmount:number; }
export interface UpdateBudgetRequest { plannedAmount:number; }
export interface BudgetSummaryBreakdown { budgetId:string; categoryId:string; categoryName:string; categoryActive:boolean; plannedAmount:number|string; actualAmount:number|string; remainingAmount:number|string; consumptionPercentage:number|string|null; status:'ON_TRACK'|'WARNING'|'EXCEEDED'; }
export interface BudgetSummary { month:string; totalPlanned:number|string; totalActual:number|string; totalRemaining:number|string; totalConsumptionPercentage:number|string|null; budgets:BudgetSummaryBreakdown[]; }
