export type CategoryApplicability = 'EXPENSE' | 'INCOME' | 'BOTH';

export interface Category {
  id: string;
  name: string;
  applicability: CategoryApplicability;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryRequest {
  name: string;
  applicability: CategoryApplicability;
}

export const CATEGORY_APPLICABILITY_OPTIONS: ReadonlyArray<{
  value: CategoryApplicability;
  label: string;
}> = [
  { value: 'EXPENSE', label: 'Despesas' },
  { value: 'INCOME', label: 'Receitas' },
  { value: 'BOTH', label: 'Despesas e receitas' },
];

export function categoryApplicabilityLabel(value: CategoryApplicability): string {
  return CATEGORY_APPLICABILITY_OPTIONS.find((option) => option.value === value)?.label ?? 'Despesas e receitas';
}
