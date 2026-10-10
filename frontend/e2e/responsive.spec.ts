import { expect, test } from './fixtures/auth';

test.describe('responsive financial shell', () => {
  test('keeps dashboard within the viewport', async ({ page, authenticatedPage }) => {
    await expect(page).toHaveURL(/dashboard/);
    await expect(page.getByRole('main')).toBeVisible();

    const dimensions = await page.evaluate(() => ({
      bodyWidth: document.body.scrollWidth,
      viewportWidth: window.innerWidth,
    }));
    expect(dimensions.bodyWidth).toBeLessThanOrEqual(dimensions.viewportWidth);
  });

  test('renders the transaction history on mobile without horizontal overflow', async ({
    page,
    authenticatedPage,
  }) => {
    if ((page.viewportSize()?.width ?? 1280) < 768) {
      await page.getByRole('button', { name: 'Abrir ou recolher menu de navegação' }).click();
    }
    await page.locator('.nav-expansion-header').filter({ hasText: 'Transações' }).click();
    await page.getByRole('link', { name: /todas as transações/i }).click();
    await expect(page.getByRole('main')).toBeVisible();

    const dimensions = await page.evaluate(() => ({
      bodyWidth: document.body.scrollWidth,
      viewportWidth: window.innerWidth,
    }));
    expect(dimensions.bodyWidth).toBeLessThanOrEqual(dimensions.viewportWidth);
  });
});
