import { expect, test } from './fixtures/auth';

const reportRequest = (page: import('@playwright/test').Page) =>
  page.waitForRequest((request) => request.url().includes('/api/reports/financial'));

const openFinancialReport = async (page: import('@playwright/test').Page) => {
  const width = page.viewportSize()?.width ?? 1280;
  if (width < 768) {
    const menuButton = page.getByRole('button', { name: 'Abrir ou recolher menu de navegação' });
    if ((await menuButton.getAttribute('aria-expanded')) !== 'true') await menuButton.click();
    await page.locator('a[href="/reports/financial"]').evaluate((element) => (element as HTMLElement).click());
    return;
  }
  await page.locator('.nav-expansion-header').filter({ hasText: 'Relatórios' }).click();
  await page.getByRole('link', { name: 'Financeiro', exact: true }).click();
};

const assertReportRequest = (request: import('@playwright/test').Request, hasAccount = false) => {
  const url = new URL(request.url());
  expect(request.method()).toBe('GET');
  expect(url.searchParams.get('from')).toBeTruthy();
  expect(url.searchParams.get('to')).toBeTruthy();
  expect(url.searchParams.get('page')).toBeTruthy();
  expect(url.searchParams.get('size')).toBeTruthy();
  expect(url.searchParams.has('ownerId')).toBeFalsy();
  expect(url.searchParams.has('userId')).toBeFalsy();
  if (hasAccount) expect(url.searchParams.getAll('accountIds').length).toBeGreaterThan(0);
};

test.describe('Relatório financeiro', () => {
  test('carrega, aplica filtros e restaura no desktop', async ({ page, authenticatedPage }) => {
    await expect(page).toHaveURL(/dashboard/);
    const initialRequest = reportRequest(page);
    await openFinancialReport(page);
    await expect(page.locator('h1.page-title')).toBeVisible({ timeout: 15000 });
    await expect(page.getByRole('button', { name: 'Aplicar filtros' })).toBeVisible();
    await expect(page.getByText('Saldo inicial', { exact: true })).toBeVisible();
    const initial = await initialRequest;
    expect(initial.url()).toContain('/api/reports/financial');
    assertReportRequest(initial);

    const accountSelect = page.locator('mat-select').first();
    if ((page.viewportSize()?.width ?? 1280) < 768) {
      await page.getByRole('button', { name: 'Abrir ou recolher menu de navegação' }).click({ force: true });
      await page.waitForTimeout(250);
    }
    await accountSelect.click({ force: true });
    const accountOptions = page.locator('mat-option');
    const optionCount = await accountOptions.count();
    if (optionCount > 1) {
      await accountOptions.nth(1).click();
      await page.getByRole('button', { name: 'Aplicar filtros' }).click();
      const filtered = await reportRequest(page);
      assertReportRequest(filtered, true);
    }
    await expect(page.getByText('Resultado financeiro', { exact: true })).toBeVisible();
    if ((page.viewportSize()?.width ?? 1280) < 768) return;

    const from = page.getByLabel('Data inicial no formato dia, mês e ano');
    const to = page.getByLabel('Data final no formato dia, mês e ano');
    await from.fill('2026-10-20');
    await to.fill('2026-10-01');
    await page.getByRole('button', { name: 'Aplicar filtros' }).click({ force: true });
    await expect(page.getByLabel('Data inicial no formato dia, mês e ano')).toHaveValue('19/10/2026');
    await expect(page.getByLabel('Data final no formato dia, mês e ano')).toHaveValue('30/09/2026');

    await page.getByRole('button', { name: 'Restaurar' }).click({ force: true });
    await expect(page.getByLabel('Data inicial no formato dia, mês e ano')).toHaveValue(/\d{2}\/\d{2}\/\d{4}/);
    await expect(page.getByLabel('Data final no formato dia, mês e ano')).toHaveValue(/\d{2}\/\d{2}\/\d{4}/);
  });

  test('mantém filtros e resumo utilizáveis no mobile', async ({ page, authenticatedPage }) => {
    await expect(page).toHaveURL(/dashboard/);
    const request = reportRequest(page);
    await openFinancialReport(page);
    await expect(page.locator('h1.page-title')).toBeVisible({ timeout: 15000 });
    await expect(page.getByRole('button', { name: 'Aplicar filtros' })).toBeVisible();
    await expect(page.getByText('Saldo final', { exact: true })).toBeVisible();
    const responseRequest = await request;
    assertReportRequest(responseRequest);
    const dimensions = await page.evaluate(() => ({ bodyWidth: document.body.scrollWidth, viewportWidth: window.innerWidth }));
    expect(dimensions.bodyWidth).toBeLessThanOrEqual(dimensions.viewportWidth);
  });
});
