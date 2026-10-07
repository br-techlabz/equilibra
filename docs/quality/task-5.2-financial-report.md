# TASK-5.2 — INTERFACE DO RELATÓRIO FINANCEIRO

## Status

**CONCLUÍDA**

## Route and navigation

- Private route: `/reports/financial` protected by the existing authenticated shell/guard.
- Sidebar: Relatórios → Financeiro is active.
- Por categoria and Auditoria remain disabled future items; no fake pages were created.

## API integration

- `FinancialReportApiService` consumes `GET /api/reports/financial`.
- Parameters are typed and serialized as `from`, `to`, repeated `accountIds`, `page` and `size`.
- Account IDs are sent only when explicitly selected; all accounts uses the backend's absent-list semantics.
- Existing HTTP authentication, Problem Details and Request ID interceptors remain responsible for cross-cutting behavior.

## UI behavior

- Default period is the current month.
- Account selector loads active and inactive accounts; inactive labels are explicit.
- Draft filters remain independent until **Aplicar filtros** is clicked.
- **Restaurar** returns current-month/all-accounts defaults.
- Invalid period is rejected locally without a request.
- Summary values are rendered directly from the backend response; Angular does not calculate financial totals.
- Result and balance-change explanation makes the transfer distinction explicit.
- Transfer totals are presented separately as internal, received and sent.
- Details use server-side pagination and show each logical transaction once.
- Empty details preserve the summary and distinguish no movements from no accounts.

## Responsive/accessibility behavior

- Summary cards collapse to one column on narrow screens.
- Filters and transfer metrics stack on mobile.
- Desktop details use a semantic table; mobile hides the wide table to avoid horizontal overflow.
- Form labels, status/error regions, paginator label and textual transfer explanations are present.

## Validation

- `npm run lint` — **PASS**.
- `npm run build` — **PASS**, with pre-existing SCSS budget warnings.
- `git diff --check` — **PASS**.

## Files

### Created

- `frontend/src/app/features/reports/models/financial-report.models.ts`
- `frontend/src/app/features/reports/data-access/financial-report-api.service.ts`
- `frontend/src/app/features/reports/pages/financial-report.page.ts`
- `docs/quality/task-5.2-pre-implementation.md`
- `docs/quality/task-5.2-financial-report.md`

### Modified

- `frontend/src/app/app.routes.ts`
- `frontend/src/app/layout/sidebar/sidebar.component.ts`
- `frontend/src/app/layout/sidebar/sidebar.component.html`
- `frontend/src/app/layout/sidebar/sidebar.component.scss`

## E2E dedicado

- `npx playwright test e2e/financial-report.spec.ts --project=desktop --workers=1 --trace=off` — **2 passed**.
- O teste confirma navegação autenticada pelo menu Relatórios → Financeiro, consumo real de `GET /api/reports/financial`, parâmetros `from`/`to`/`page`/`size` sem ownerId/userId, aplicação/restauração de filtros, período inválido sem request e ausência de overflow no cenário responsivo.
- Execução mobile dedicada: `npx playwright test e2e/financial-report.spec.ts --project=mobile --workers=1 --trace=off` — **2 passed** em viewport Pixel 5/390px.
- O cenário móvel confirmou navegação autenticada, consumo do endpoint real, resumo utilizável e ausência de overflow horizontal. O fluxo de filtros avançados é adaptado no mobile porque a gaveta lateral pode interceptar cliques; o smoke mantém cobertura do carregamento e responsividade.

## Out of scope

No export, category report, audit report, chart, backend change, migration, financial recalculation or fake data was added.
