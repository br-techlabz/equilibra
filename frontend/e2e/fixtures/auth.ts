import { expect, test as base } from '@playwright/test';

export const test = base.extend<{ authenticatedPage: void }>({
  authenticatedPage: async ({ page }, use, testInfo) => {
    const network: string[] = [];
    page.on('request', (request) => {
      if (request.url().includes('/api/')) {
        const line = `>> ${request.method()} ${request.url()}`;
        network.push(line);
        console.log(`[e2e-network] ${line}`);
      }
    });
    page.on('response', (response) => {
      if (response.url().includes('/api/')) {
        const line = `<< ${response.status()} ${response.url()}`;
        network.push(line);
        console.log(`[e2e-network] ${line}`);
      }
    });
    const email = `e2e-${Date.now()}-${Math.random().toString(36).slice(2)}@example.test`;
    const password = 'senhaE2EValida123';

    const registration = await page.request.post('/api/auth/register', {
      data: { email, password },
    });
    expect(registration.ok()).toBeTruthy();

    const login = await page.request.post('/api/auth/login', { data: { email, password } });
    expect(login.ok()).toBeTruthy();
    const loginPayload = await login.json() as { accessToken: string; tokenType: string };
    const currentUser = await page.request.get('/api/users/me', {
      headers: { Authorization: `${loginPayload.tokenType} ${loginPayload.accessToken}` },
    });
    expect(currentUser.ok()).toBeTruthy();
    const currentUserPayload = await currentUser.json();

    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(loginPayload) });
    });
    await page.route('**/api/users/me', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(currentUserPayload) });
    });

    await page.goto('/login');
    await page.getByLabel('E-mail').fill(email);
    await page.getByLabel('Senha', { exact: true }).fill(password);
    await page.getByRole('button', { name: /entrar/i }).click();
    await expect(page).toHaveURL(/dashboard/, { timeout: 15_000 });

    try {
      await use();
    } finally {
      await testInfo.attach('auth-network.log', {
        body: network.join('\n'),
        contentType: 'text/plain',
      });
    }
  },
});

export { expect };
