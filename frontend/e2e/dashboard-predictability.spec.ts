import { expect, test } from './fixtures/auth';
test('exibe previsibilidade separada do realizado', async ({ page, authenticatedPage }) => {
 await page.route('**/api/cash-flow/projection**', async r => r.fulfill({status:200,contentType:'application/json',body:JSON.stringify({from:'2026-10-01',to:'2026-10-10',openingBalance:5000,projectedIncome:3500,projectedExpense:2300,projectedChange:1200,closingBalance:6200,minimumBalance:4700,series:[],events:[]})}));
 await page.route('**/api/commitments/indicators**', async r => r.fulfill({status:200,contentType:'application/json',body:JSON.stringify({referenceDate:'2026-10-10',upcomingDays:7,overdue:{count:1,amount:200},dueToday:{count:0,amount:0},upcoming:{count:2,amount:300},futurePending:{count:1,amount:500}})}));
 await expect(page).toHaveURL(/dashboard/);
 await expect(page.getByText('Previsibilidade Financeira',{exact:true})).toBeVisible();
 await expect(page.getByText(/R\$\s?[\d.]+,\d{2}/).nth(0)).toBeVisible();
 await expect(page.getByText(/R\$\s?[\d.]+,\d{2}/).nth(1)).toBeVisible();
 await expect(page.getByText(/R\$\s?[\d.]+,\d{2}/).nth(2)).toBeVisible();
 expect(await page.evaluate(()=>performance.getEntriesByType('resource').some(e=>e.name.includes('ownerId')))).toBe(false);
});
