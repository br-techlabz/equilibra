export type AssetAccountType =
  | 'CHECKING'
  | 'SAVINGS'
  | 'CASH'
  | 'INVESTMENT'
  | 'DIGITAL'
  | 'OTHER';

export interface AssetAccount {
  id: string;
  name: string;
  type: AssetAccountType;
  initialBalance: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AssetAccountRequest {
  name: string;
  type: AssetAccountType;
  initialBalance: number;
}

export const ASSET_ACCOUNT_TYPE_OPTIONS: ReadonlyArray<{
  value: AssetAccountType;
  label: string;
  icon: string;
}> = [
  { value: 'CHECKING', label: 'Conta corrente', icon: 'account_balance' },
  { value: 'SAVINGS', label: 'Poupança', icon: 'savings' },
  { value: 'CASH', label: 'Dinheiro', icon: 'payments' },
  { value: 'INVESTMENT', label: 'Investimento', icon: 'trending_up' },
  { value: 'DIGITAL', label: 'Conta digital', icon: 'account_balance_wallet' },
  { value: 'OTHER', label: 'Outros', icon: 'more_horiz' },
];

export function assetAccountTypeOption(type: AssetAccountType) {
  return ASSET_ACCOUNT_TYPE_OPTIONS.find((option) => option.value === type) ?? {
    value: type,
    label: 'Outros',
    icon: 'account_balance_wallet',
  };
}
