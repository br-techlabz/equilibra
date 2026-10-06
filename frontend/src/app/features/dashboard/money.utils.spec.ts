import { formatCentsAsBRL, sumMoneyInCents, toCents } from './money.utils';

describe('dashboard money utils', () => {
  it('converts decimal values to cents without floating point errors', () => {
    expect(toCents(0.1)).toBe(10);
    expect(toCents('0,20')).toBe(20);
    expect(sumMoneyInCents([0.1, 0.2])).toBe(30);
  });

  it('preserves negative values', () => {
    expect(sumMoneyInCents([1000, -350.5])).toBe(64950);
    expect(sumMoneyInCents([-100, -200])).toBe(-30000);
  });

  it('formats cents as Brazilian currency', () => {
    expect(formatCentsAsBRL(30030)).toBe('R$ 300,30');
  });
});
