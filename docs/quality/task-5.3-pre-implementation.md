# TASK-5.3 — PRE-IMPLEMENTATION REPORT

## Contrato

Endpoint privado previsto pelo ADR:

```text
GET /api/reports/categories
```

Parâmetros: `from`, `to` obrigatórios; `accountIds` repetidos opcionais. Ausência/vazio seleciona todas as contas próprias. IDs duplicados são deduplicados e IDs estrangeiros/inexistentes retornam 404. O período usa `occurredAt` e `[from,to)`.

## Agrupamentos

O schema atual exige `category_id` em toda `FinancialTransaction`, portanto não haverá grupo fictício “Sem categoria”. O nome atual e applicability da Category serão usados; categorias inativas históricas continuam visíveis. `BOTH` mantém income e expense separados.

Response terá summary global (`incomeTotal`, `expenseTotal`, `netResult`) e agrupamentos com `categoryId`, `categoryName`, `applicability`, `incomeTotal`, `expenseTotal` e `netResult`. Transferências e CANCELLED não entram.

## Query strategy

Agregação owner-scoped no banco por projeção/consulta dedicada, com `GROUP BY` por categoryId. Account filter usa source/destination conforme seleção. Não carregar todas as transações em Java, não fazer N+1 e não juntar tags/anexos de modo multiplicador. Índices V5 de owner/type/status/category/occurredAt serão reutilizados; nenhuma migration é prevista.

## Segurança

Owner exclusivamente de `CurrentUser`. Requests não aceitam ownerId/userId. Problem Details/Request ID existentes serão reutilizados. DTOs não expõem entidades JPA.

## Testes

Cobrir categorias EXPENSE/INCOME/BOTH, inactive/rename, período boundaries, todas/uma/várias contas, inactive/cross-owner, cancelamento, transferências neutras, precisão decimal, isolamento, reconciliação com Financial Report 5.1 e Testcontainers/MySQL.

## Arquivos

Criar módulo `backend/src/main/java/br/com/equilibra/report/category/` (api/application/infrastructure), testes do módulo e relatório final. Não alterar frontend, ledger, migrations ou implementar TASK-5.4.
