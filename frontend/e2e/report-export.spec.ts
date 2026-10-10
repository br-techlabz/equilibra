import { expect, test } from './fixtures/auth';

const openReport = async (page: import('@playwright/test').Page, label: string) => {
  if ((page.viewportSize()?.width ?? 1280) < 768) {
    await page.getByRole('button', { name: 'Abrir ou recolher menu de navegação' }).click();
    await page.locator(`a[href^="/reports/"]`).filter({ hasText: label }).evaluate((element) => (element as HTMLElement).click());
    return;
  }
  await page.locator('.nav-expansion-header').filter({ hasText: 'Relatórios' }).click();
  await page.getByRole('link', { name: label, exact: true }).click();
};

test.describe('Exportação dos relatórios', () => {
  for (const report of [
    { label: 'Financeiro', title: 'Relatório financeiro', slug: 'financial' },
    { label: 'Por categoria', title: 'Relatório por categoria', slug: 'categories' },
    { label: 'Auditoria', title: 'Relatório de auditoria', slug: 'audit' },
  ]) {
    test(`${report.label} exporta CSV e PDF com filtros aplicados`, async ({ page, authenticatedPage }) => {
      await expect(page).toHaveURL(/dashboard/);
      await openReport(page, report.label);
      await expect(page.getByText(report.title, { exact: true })).toBeVisible({ timeout: 15000 });
      for (const format of ['CSV', 'PDF']) {
        const responsePromise = page.waitForResponse(response => response.url().includes(`/api/reports/${report.slug}/export`));
        await page.getByRole('button', { name: 'Exportar' }).click();
        await page.getByRole('menuitem', { name: format }).click();
        const response = await responsePromise;
        expect(response.status()).toBe(200);
        const request = response.request();
        const url = new URL(request.url());
        expect(url.searchParams.get('format')).toBe(format);
        expect(url.searchParams.get('from')).toBeTruthy();
        expect(url.searchParams.get('to')).toBeTruthy();
        expect(url.searchParams.has('ownerId')).toBeFalsy();
        expect(response.headers()['content-disposition']).toContain('attachment');
      }
    });
  }
});
