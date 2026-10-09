# SPRINT 5 — PRE-GATE/GATE DE APROVAÇÃO

## Status final

**APPROVED — evidências técnicas e E2E executadas.**

## Tasks verificadas

- TASK-5.0: arquitetura/contratos dos relatórios.
- TASK-5.1/5.2: relatório financeiro backend/interface.
- TASK-5.3/5.4: relatório por categoria backend/interface.
- TASK-5.5/5.6: relatório de auditoria backend/interface.
- TASK-5.7: exportação PDF/CSV dos três relatórios.

## Inventário

Endpoints JSON:

- `GET /api/reports/financial`
- `GET /api/reports/categories`
- `GET /api/reports/audit`

Endpoints de exportação:

- `GET /api/reports/financial/export?format=PDF|CSV`
- `GET /api/reports/categories/export?format=PDF|CSV`
- `GET /api/reports/audit/export?format=PDF|CSV`

Interfaces Angular:

- `financial-report.page.ts`
- `category-report.page.ts`
- `audit-report.page.ts`

## Segurança e ownership

- Endpoints protegidos por JWT.
- Identidade derivada de `CurrentUser`.
- Nenhum `ownerId` aceito pelo cliente.
- Contas e categorias resolvidas com ownership.
- Playwright confirmou ausência de `ownerId` nas URLs de exportação.
- CSV Formula Injection neutralizada no renderer.
- PDFs/CSVs usam `Cache-Control: no-store`.

## Exportações

As seis combinações foram exercitadas via Playwright:

- Financeiro CSV/PDF: HTTP 200.
- Categorias CSV/PDF: HTTP 200.
- Auditoria CSV/PDF: HTTP 200.

Foram confirmados `format`, `from`, `to` e `Content-Disposition` com attachment.

## Filtros e semântica

- Período com intervalo `[from,to)`.
- Contas selecionadas preservadas.
- Auditoria suporta tipo, status, categoria e tags no contrato.
- Exportação de auditoria percorre páginas internas até o limite de 5.000 registros.
- A UI permanece paginada, enquanto a exportação representa o conjunto filtrado.

## Segurança financeira

- Valores mantidos em `BigDecimal` no backend.
- Transferências não são somadas como receitas/despesas.
- Totais derivam dos query services existentes.
- Cancelamentos respeitam status do relatório.
- Relatórios e exportações permanecem owner-scoped.

## Testes executados

Backend:

```bash
cd backend
./mvnw test -q
./mvnw -q -Dtest=ReportExportRendererTest test
```

Resultado: PASS.

Frontend:

```bash
cd frontend
npm test -- --watch=false
npm run build
npm run lint
```

Resultados:

- Unit tests: `29 SUCCESS`.
- Build: PASS.
- Lint: PASS.

Playwright:

```bash
npx playwright test e2e/report-export.spec.ts --project=desktop
```

Resultado:

```text
3 passed (16.5s)
```

O teste foi executado com backend em heap aumentado (`MAVEN_OPTS=-Xmx2g`) para evitar exaustão durante geração PDF.

## Falhas ambientais corrigidas

- Primeira execução Playwright encontrou `OutOfMemoryError: Java heap space` no PDFBox durante exportação de categorias.
- Backend foi reiniciado com `MAVEN_OPTS=-Xmx2g`.
- Nova execução dos três cenários passou integralmente.

## Severidade

- CRITICAL aberto: nenhum.
- HIGH aberto: nenhum.
- MEDIUM/LOW: avisos de budget de estilos e optional chaining no build Angular; não bloqueiam a Sprint 5.

## Decisão

**SPRINT 5 — APPROVED**

READY FOR SPRINT 6 PLANNING.

A TASK-5.8 foi executada como gate; nenhuma funcionalidade da Sprint 6 foi iniciada.
