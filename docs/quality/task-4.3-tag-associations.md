# TASK-4.3 — Associação de Tags às Transações

## Status

**CONCLUÍDA**

## Implementado

- migration V7 `financial_transaction_tags` com chave composta e FKs;
- associação many-to-many na `FinancialTransaction` lógica;
- `TagSummary` e leitura owner-aware em batch por transação;
- `TransactionTagService` validando ownership, duplicidade e Tag ativa;
- `tagIds` opcionais em requests de Expense, Income e Transfer;
- criação e atualização das três transações com associação atômica;
- responses de Expense, Income e Transfer com summaries de Tags reais;
- seletor Angular reutilizável e integração nos formulários de Expense, Income e Transfer;
- ausência de Tags continua válida;
- Attachments e filtro completo do Histórico permanecem fora do escopo.

## Segurança e invariantes

- Tags de outro owner retornam 404;
- Tags inativas não podem ser novas associações;
- IDs duplicados são rejeitados;
- transação cancelada mantém imutabilidade;
- associação não altera amount, status, contas, movimentos, saldo, net worth ou Dashboard;
- Transfer possui um único conjunto de Tags.

## Testes executados

- `TagControllerIntegrationTest`: 3 testes, 0 falhas;
- `ExpenseControllerIntegrationTest`: 3 testes, 0 falhas;
- `GoldenScenarioIntegrationTest`: 2 testes, 0 falhas;
- testes Angular: 27 SUCCESS;
- frontend lint: PASS;
- frontend build: PASS;
- `git diff --check`: PASS.

## Fora de escopo

- Attachments, storage, upload/download;
- filtro do Histórico por Tag;
- Dashboard por Tag;
- criação inline de Tags;
- cores, ícones e hierarquia de Tags.
