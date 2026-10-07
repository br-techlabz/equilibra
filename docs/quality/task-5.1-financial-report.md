# TASK-5.1 — BACKEND DO RELATÓRIO FINANCEIRO

## Status

**CONCLUÍDA**

## ADR

Arquivo: `docs/adr/0004-financial-reports-architecture.md`

Conformidade: **PASS** para o contrato implementado. O endpoint é um read model privado, sem segundo ledger, sem totais persistidos e sem alteração das fundações financeiras.

## Endpoint

```text
GET /api/reports/financial
```

Authentication: JWT obrigatório; owner obtido de `CurrentUser`. O request não aceita ownerId, userId ou email.

## Filters

- `from` e `to`: obrigatórios, ISO date-time;
- período: `[from,to)`, baseado em `occurredAt`;
- `accountIds`: ausente/vazio = todas as contas próprias; repetidos deduplicados; contas ativas e inativas próprias são aceitas;
- `page` default 0 e `size` default 20, máximo 100;
- categoria/tag não foram adicionados ao contrato mínimo, conforme ADR desta task.

## Formulas

```text
openingBalance = initialBalance selecionado + efeitos ACTIVE anteriores a from
financialResult = incomeTotal - expenseTotal
balanceChange = incomeTotal - expenseTotal
              + incomingBoundaryTransfers - outgoingBoundaryTransfers
closingBalance = openingBalance + balanceChange
```

Income e Expense são somados somente quando a conta correspondente pertence à seleção. Transferências internas são separadas e têm efeito agregado zero; transferências de fronteira são métricas próprias e nunca Income/Expense. CANCELLED não produz efeito.

## Details

Detalhes são paginados no banco e ordenados por `occurredAt DESC, id DESC`. Cada `FinancialTransaction` lógica aparece uma vez; o DTO não expõe entidades JPA, ownerId ou metadata de storage.

## Query/performance

Foram criados `FinancialReportRepository` e `FinancialReportQueryService` dedicados. Queries são owner-scoped e usam `Pageable` para detalhes. O summary é calculado sobre o conjunto completo filtrado, independente da página. Nenhuma migration foi necessária; índices V5 existentes são reutilizados.

## Evidências

- compilação backend: **BUILD SUCCESS**;
- `./mvnw verify`: **206 testes, 0 falhas, 0 erros; BUILD SUCCESS**;
- Testcontainers/MySQL 8.0 validou as migrations V2–V8;
- regressões existentes de Sprint 3/4 incluídas na suíte passaram.

## Arquivos principais

### Criados

- `backend/src/main/java/br/com/equilibra/report/api/FinancialReportController.java`
- `backend/src/main/java/br/com/equilibra/report/api/FinancialReportResponse.java`
- `backend/src/main/java/br/com/equilibra/report/api/FinancialReportTransaction.java`
- `backend/src/main/java/br/com/equilibra/report/application/FinancialReportQueryService.java`
- `backend/src/main/java/br/com/equilibra/report/infrastructure/FinancialReportRepository.java`
- `docs/quality/task-5.1-pre-implementation.md`
- `docs/quality/task-5.1-financial-report.md`

### Modificados

Nenhuma fundação financeira, frontend ou migration foi alterada.

## Fora de escopo

Frontend, relatório por categoria, relatório de auditoria, PDF/CSV/XLSX, gráficos, cache, snapshots e migrations novas permanecem para tasks seguintes.
