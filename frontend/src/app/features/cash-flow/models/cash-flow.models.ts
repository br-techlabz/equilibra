export interface CashFlowPoint { date:string; balance:number|string; }
export interface CashFlowEvent { commitmentId:string; dueDate:string; description:string; type:'EXPENSE'|'INCOME'; amount:number|string; accountId:string; categoryId:string; }
export interface CashFlowProjection { from:string; to:string; openingBalance:number|string; projectedIncome:number|string; projectedExpense:number|string; projectedChange:number|string; closingBalance:number|string; minimumBalance:number|string; series:CashFlowPoint[]; events:CashFlowEvent[]; }
