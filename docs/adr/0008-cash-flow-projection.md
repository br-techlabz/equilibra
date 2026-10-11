# ADR 0008 — Projeção de Fluxo de Caixa

## Status

Aceito para a TASK-7.5.

## Decisões

- Projeção é consultiva e read-only; não altera ledger nem cria transações.
- Saldo inicial vem do `AccountBalanceQueryService` para contas ativas selecionadas.
- Somente `PENDING` commitments elegíveis entram; `SETTLED` já está no ledger e `CANCELLED` é excluído.
- Horizonte máximo: 12 meses; resolução diária; datas são `LocalDate` civis.
- Despesas reduzem e receitas aumentam saldo projetado.
- Transferências previstas não existem no modelo atual e não são inventadas.
- Ownership é derivado de `CurrentUser`.
