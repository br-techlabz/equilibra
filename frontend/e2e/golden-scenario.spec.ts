import { expect, test } from './fixtures/auth';

test.describe('Golden Scenario financeiro', () => {
  test('navega pelas áreas financeiras autenticadas', async ({ page, authenticatedPage }) => {
    await expect(page).toHaveURL(/dashboard/);
    await expect(page.getByRole('main').first()).toBeVisible();

    const isMobile = (await page.viewportSize())?.width !== undefined && (await page.viewportSize())!.width < 768;
    if (isMobile) {
      await page.goto('/accounts');
    } else {
      await page.getByRole('link', { name: 'Contas', exact: true }).click();
    }
    await expect(page).toHaveURL(/accounts/);
    await expect(page.getByRole('main').first()).toBeVisible();

    if (isMobile) {
      await page.goto('/categories');
    } else {
      await page.getByRole('link', { name: 'Categorias', exact: true }).click();
    }
    await expect(page).toHaveURL(/categories/);
    await expect(page.getByRole('main').first()).toBeVisible();

    if (isMobile) {
      await page.goto('/transactions');
    } else {
      await page.locator('.nav-expansion-header').filter({ hasText: 'Transações' }).click();
      await page.getByRole('link', { name: /todas as transações/i }).click();
    }

    await expect(page).toHaveURL(/transactions/);
    await expect(page.getByRole('main').first()).toBeVisible();
    if (!isMobile) {
      await expect(page.locator('app-content-panel').first()).toBeVisible();
    }
  });
});
