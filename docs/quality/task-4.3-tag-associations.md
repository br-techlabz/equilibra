# TASK-4.3 — Associação de Tags às Transações

## Status

**CONCLUÍDA**

## Implementado

- migration V7 `financial_transaction_tags`;
- associação many-to-many na `FinancialTransaction` lógica;
- `TagSummary` e leitura owner-aware das Tags associadas;
- `TransactionTagService` validando ownership, duplicidade e Tag ativa;
- `tagIds` opcionais em requests de Expense, Income e Transfer;
- criação e atualização das três transações com associação atômica;
- responses de Expense, Income e Transfer com summaries reais de Tags;
- `TagSelectorComponent` reutilizável;
- integração do seletor nos formulários de Expense, Income e Transfer;
- seleção múltipla, remoção e preservação dos IDs selecionados;
- ausência de Tags continua válida;
- nenhuma alteração nas regras financeiras do ledger.

## Segurança e invariantes

- Tag de outro owner retorna 404;
- Tag inexistente retorna 404;
- Tag inativa não pode ser nova associação;
- IDs duplicados são rejeitados;
- transação cancelada mantém imutabilidade;
- associação não altera amount, status, contas, movimentos, saldo, net worth ou Dashboard;
- Transfer possui um único conjunto de Tags;
- Attachments e filtro completo do Histórico permanecem fora do escopo.

## Testes executados

- `TagControllerIntegrationTest`: 3 testes, 0 falhas;
- `ExpenseControllerIntegrationTest`: 3 testes, 0 falhas;
- `GoldenScenarioIntegrationTest`: 2 testes, 0 falhas;
- testes Angular: 27 SUCCESS;
- frontend lint: PASS;
- frontend build: PASS;
- backend compilação: PASS;
- `git diff --check`: PASS.

## Fora de escopo

Attachments, storage, upload/download, filtro do Histórico por Tag, Dashboard por Tag, criação inline de Tags e customizações visuais avançadas.
