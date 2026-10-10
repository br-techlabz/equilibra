import { expect, test } from './fixtures/auth';

const commitment = (overrides: Record<string, unknown> = {}) => ({
  id: 'c1',
  type: 'EXPENSE',
  description: 'Internet residencial',
  plannedAmount: 129.9,
  dueDate: '2026-10-10',
  accountId: 'a1',
  categoryId: 'cat1',
  recurrenceRuleId: null,
  status: 'PENDING',
  financialTransactionId: null,
  settledAt: null,
  overdue: false,
  createdAt: '2026-10-01T00:00:00Z',
  updatedAt: '2026-10-01T00:00:00Z',
  ...overrides,
});

const openCalendar = async (page: import('@playwright/test').Page) => {
  if ((page.viewportSize()?.width ?? 1280) < 768) {
    await page.getByRole('button', { name: 'Abrir ou recolher menu de navegação' }).click();
  }
  await page.locator('a[href="/financial-calendar"]').click({ force: true });
};

test.describe('Agenda Financeira', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/asset-accounts?includeInactive=false', async route => route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([{ id: 'a1', name: 'Conta corrente', active: true, type: 'CHECKING', initialBalance: 1000 }]),
    }));
    await page.route('**/api/categories?includeInactive=false', async route => route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([{ id: 'cat1', name: 'Casa', active: true, applicability: 'EXPENSE' }]),
    }));
  });

  test('exibe compromissos no mês e filtra por tipo', async ({ page, authenticatedPage }) => {
    await page.route('**/api/commitments?*', async route => route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ content: [commitment(), commitment({ id: 'c2', type: 'INCOME', description: 'Salário', plannedAmount: 3500, dueDate: '2026-10-05' })], totalElements: 2, totalPages: 1, number: 0, size: 100 }),
    }));
    await openCalendar(page);
    await expect(page.getByRole('heading', { name: 'Agenda Financeira' })).toBeVisible();
    await expect(page.getByText('Internet residencial', { exact: true })).toBeVisible();
    await expect(page.getByText('R$ 129,90', { exact: true })).toBeVisible();
    await expect(page.getByText('Salário', { exact: true })).toBeVisible();
    await page.getByLabel('Tipo').click();
    await page.getByRole('option', { name: 'Despesas' }).click();
    await expect(page.getByText('Internet residencial', { exact: true })).toBeVisible();
    await expect(page.getByText('Salário', { exact: true })).not.toBeVisible();
    expect(await page.evaluate(() => performance.getEntriesByType('resource').some(entry => entry.name.includes('ownerId')))).toBe(false);
  });

  test('mostra o compromisso selecionado no dia e permite cancelamento', async ({ page, authenticatedPage }) => {
    await page.route('**/api/commitments?*', async route => route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ content: [commitment()], totalElements: 1, totalPages: 1, number: 0, size: 100 }),
    }));
    let cancelled = false;
    await page.route('**/api/commitments/c1/cancel', async route => {
      cancelled = true;
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(commitment({ status: 'CANCELLED' })) });
    });
    await openCalendar(page);
    await page.getByRole('button', { name: /2026-10-10, 1 compromissos/ }).click();
    await expect(page.getByText('Internet residencial', { exact: true })).toBeVisible();
    page.once('dialog', dialog => dialog.accept());
    await page.getByRole('button', { name: 'Cancelar' }).click();
    await expect.poll(() => cancelled).toBe(true);
  });
});
