import { expect, test } from './fixtures/auth';

test.describe('responsive financial shell', () => {
  test('keeps dashboard within the viewport', async ({ page, authenticatedPage }) => {
    await page.goto('/dashboard');
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
    await page.goto('/transactions');
    await expect(page.getByRole('main')).toBeVisible();

    const dimensions = await page.evaluate(() => ({
      bodyWidth: document.body.scrollWidth,
      viewportWidth: window.innerWidth,
    }));
    expect(dimensions.bodyWidth).toBeLessThanOrEqual(dimensions.viewportWidth);
  });
});
