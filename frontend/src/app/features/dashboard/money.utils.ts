export function toCents(value: number | string): number {
  const normalized = String(value).trim().replace(',', '.');
  const [whole = '0', fraction = ''] = normalized.split('.');
  const sign = whole.startsWith('-') ? -1 : 1;
  const unsignedWhole = whole.replace('-', '') || '0';
  const cents = Number(`${unsignedWhole}${fraction.padEnd(2, '0').slice(0, 2)}`);
  return sign * cents;
}

export function sumMoneyInCents(values: ReadonlyArray<number | string>): number {
  return values.reduce<number>((total, value) => total + toCents(value), 0);
}

export function formatCentsAsBRL(cents: number): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cents / 100);
}
