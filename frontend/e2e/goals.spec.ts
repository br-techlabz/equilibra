import { expect, test } from './fixtures/auth';

const openAuthenticatedRoute = async (page: import('@playwright/test').Page, route: string) => {
  if ((page.viewportSize()?.width ?? 1280) < 768) {
    await page.getByRole('button', { name: 'Abrir ou recolher menu de navegação' }).click();
  }
  await page.locator(`a[href="${route}"]`).click();
};

test('visualiza metas e usa filtros sem ownerId', async ({ page, authenticatedPage }) => {
  await page.route('**/api/goals', async (route) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify([{
      id: 'g1', name: 'Reserva de emergência', description: 'Objetivo', targetAmount: 10000,
      targetDate: '2027-12-31', status: 'ACTIVE', progressAmount: 3500, remainingAmount: 6500,
      progressPercentage: 35, createdAt: '2026-10-01T00:00:00Z', updatedAt: '2026-10-01T00:00:00Z',
    }]),
  }));
  await openAuthenticatedRoute(page, '/goals');
  await expect(page.getByText('Reserva de emergência', { exact: true })).toBeVisible();
  await expect(page.getByText(/35(?:,00)?%/)).toBeVisible();
  await page.getByLabel('Status').click();
  await page.getByRole('option', { name: 'Concluídas' }).click();
  expect(await page.evaluate(() => performance.getEntriesByType('resource').some((entry) => entry.name.includes('ownerId')))).toBe(false);
  const size = await page.evaluate(() => ({ body: document.body.scrollWidth, width: innerWidth }));
  expect(size.body).toEqual(size.width);
});
