import { expect, test } from './fixtures/auth';

const openAuthenticatedRoute = async (page: import('@playwright/test').Page, route: string) => {
  if ((page.viewportSize()?.width ?? 1280) < 768) {
    await page.getByRole('button', { name: 'Abrir ou recolher menu de navegação' }).click();
  }
  await page.locator(`a[href="${route}"]`).click({ force: true });
};

test.describe('Recorrências', () => {
  test('lista e filtra regras sem enviar ownerId', async ({ page, authenticatedPage }) => {
    await page.route('**/api/recurrences', async (route) => route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([
        { id: 'r1', type: 'EXPENSE', description: 'Internet residencial', amount: 129.9, categoryId: 'c1', accountId: 'a1', frequency: 'MONTHLY', startDate: '2026-10-01', endDate: null, dueDay: 10, status: 'ACTIVE', createdAt: '2026-10-01T00:00:00Z', updatedAt: '2026-10-01T00:00:00Z' },
        { id: 'r2', type: 'INCOME', description: 'Salário', amount: 3500, categoryId: 'c2', accountId: 'a1', frequency: 'MONTHLY', startDate: '2026-10-05', endDate: null, dueDay: 5, status: 'PAUSED', createdAt: '2026-10-01T00:00:00Z', updatedAt: '2026-10-01T00:00:00Z' },
      ]),
    }));
    await page.route('**/api/categories**', async (route) => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) }));
    await page.route('**/api/asset-accounts**', async (route) => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) }));
    await openAuthenticatedRoute(page, '/recurrences');
    await expect(page.getByText('Internet residencial', { exact: true })).toBeVisible();
    await expect(page.getByText('R$ 129,90', { exact: true })).toBeVisible();
    await page.getByLabel('Tipo').click();
    await page.getByRole('option', { name: 'Receitas' }).click();
    await expect(page.getByText('Salário', { exact: true })).toBeVisible();
    await expect(page.getByText('Internet residencial', { exact: true })).not.toBeVisible();
    expect(await page.evaluate(() => performance.getEntriesByType('resource').some((entry) => entry.name.includes('ownerId')))).toBe(false);
  });
});
