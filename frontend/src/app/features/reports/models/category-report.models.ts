export type CategoryApplicability = 'EXPENSE' | 'INCOME' | 'BOTH';

export interface CategoryReportGroup {
  categoryId: string;
  categoryName: string;
  applicability: CategoryApplicability;
  incomeTotal: number;
  expenseTotal: number;
  netResult: number;
}

export interface CategoryReportResponse {
  incomeTotal: number;
  expenseTotal: number;
  netResult: number;
  categories: CategoryReportGroup[];
}

export interface CategoryReportFilters {
  from: string;
  to: string;
  accountIds: string[];
}
