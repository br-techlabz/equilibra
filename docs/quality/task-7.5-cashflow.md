# TASK-7.5 — Projeção de Fluxo de Caixa

## Status

**CONCLUÍDA**

## Implementado

- ADR 0008 com decisões da projeção.
- Endpoint owner-scoped `GET /cash-flow/projection`.
- Horizonte máximo de 12 meses.
- Saldo inicial derivado de `AccountBalanceQueryService`.
- Apenas compromissos `PENDING` entram na projeção.
- Receitas, despesas, saldo final, mínimo e série diária.
- Eventos projetados tipados.
- Nenhum ledger/transação é alterado.
- OpenAPI básico.

## Validação

- `./mvnw -q -DskipTests package`: aprovado.

## Validação final

- Backend tests: aprovados.
- Backend package: aprovado.
- Frontend lint: aprovado.
- Frontend production build: aprovado.
- E2E Agenda Financeira: 2 testes aprovados em 11,0s.
- Projeção consultiva sem alteração de ledger/transações.
- Frontend `/cash-flow` implementado com gráfico SVG acessível; ECharts não foi adicionado para evitar dependência desnecessária.
- Karma unitário permanece bloqueado por infraestrutura: conecta ao ChromeHeadless, mas não apresenta resumo nem encerra dentro do timeout.

## Pendências conhecidas

Nenhuma pendência funcional conhecida dentro do escopo implementado. O bloqueio do runner Karma está documentado como limitação de infraestrutura.
