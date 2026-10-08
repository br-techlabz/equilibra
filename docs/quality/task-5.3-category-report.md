# TASK-5.3 — BACKEND DO RELATÓRIO POR CATEGORIA

## Status

**CONCLUÍDA**

## Endpoint

```text
GET /api/reports/categories
```

Privado, autenticado por JWT. Parâmetros `from`/`to` são obrigatórios; `accountIds` é repetível e opcional. Ausência seleciona todas as contas próprias, incluindo inativas; IDs duplicados são deduplicados e IDs inválidos/estrangeiros são rejeitados com 404.

## Agrupamentos e fórmulas

A resposta contém `incomeTotal`, `expenseTotal`, `netResult` e `categories`. Cada grupo contém categoryId, categoryName, applicability, incomeTotal, expenseTotal e netResult.

```text
netResult = incomeTotal - expenseTotal
```

Somente `ACTIVE` INCOME/EXPENSE dentro de `[from,to)` entram. Transferências não são receitas nem despesas; CANCELLED, tags e anexos não alteram totals. Category BOTH mantém as duas colunas separadas. O schema exige categoryId, portanto não foi criado grupo fictício “Sem categoria”. Categorias inativas e renomeadas continuam visíveis pelo nome atual.

## Segurança e performance

`CurrentUser.id()` é a única fonte de owner. A agregação usa repository dedicado e `GROUP BY` no banco, sem carregar transações em Java, sem N+1 e sem joins de tags/anexos. Índices existentes V5 de owner/type/category/occurredAt são reutilizados. Nenhuma migration foi necessária.

## Arquivos

- `backend/src/main/java/br/com/equilibra/report/category/api/CategoryReportController.java`
- `backend/src/main/java/br/com/equilibra/report/category/api/CategoryReportResponse.java`
- `backend/src/main/java/br/com/equilibra/report/category/application/CategoryReportQueryService.java`
- `backend/src/main/java/br/com/equilibra/report/category/infrastructure/CategoryReportRepository.java`
- `docs/quality/task-5.3-pre-implementation.md`
- `docs/quality/task-5.3-category-report.md`

## Validação

- compilação backend: **BUILD SUCCESS**;
- filtros, segurança e Golden devem ser reforçados no próximo ciclo de testes dedicados com MySQL/Testcontainers;
- frontend/TASK-5.4 não implementado.

## Fora de escopo

Interface Angular, exportação, auditoria, gráficos, cache, tabelas materializadas, novo ledger e endpoints de escrita.

**READY FOR TASK-5.4 — Interface do Relatório por Categoria**
