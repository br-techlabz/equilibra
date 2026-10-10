import { expect, test } from './fixtures/auth';

const openAuthenticatedRoute = async (page: import('@playwright/test').Page, route: string) => {
  if ((page.viewportSize()?.width ?? 1280) < 768) {
    await page.getByRole('button', { name: 'Abrir ou recolher menu de navegação' }).click();
  }
  await page.locator(`a[href="${route}"]`).click();
};

test.describe('Alertas de orçamento', () => {
  test('exibe estados derivados do resumo mensal sem ownerId', async ({ page, authenticatedPage }) => {
    await page.route('**/api/budgets/summary*', async route => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ month: '2026-10', totalPlanned: 1500, totalActual: 1150, totalRemaining: -150, totalConsumptionPercentage: 76.67, budgets: [{ budgetId: 'b1', categoryId: 'c1', categoryName: 'Alimentação', categoryActive: true, plannedAmount: 1000, actualAmount: 1150, remainingAmount: -150, consumptionPercentage: 115, status: 'EXCEEDED' }, { budgetId: 'b2', categoryId: 'c2', categoryName: 'Transporte', categoryActive: true, plannedAmount: 500, actualAmount: 0, remainingAmount: 500, consumptionPercentage: 0, status: 'ON_TRACK' }] }) }));
    await openAuthenticatedRoute(page, '/budgets');
    await expect(page.getByText('115%', { exact: true })).toBeVisible();
    await expect(page.getByRole('status', { name: 'Resumo dos alertas de orçamento' })).toContainText('1 excedida(s)');
    const requests = await page.evaluate(() => performance.getEntriesByType('resource').map(entry => entry.name).filter(name => name.includes('/api/budgets/summary')));
    expect(requests.every(url => !url.includes('ownerId'))).toBeTruthy();
  });
});
