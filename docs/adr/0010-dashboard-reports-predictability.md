# ADR 0010 — Integração de Previsibilidade no Dashboard

## Status

Aceito para TASK-7.7.

## Decisões

- Métricas realizadas continuam vindo do Dashboard existente.
- Saldo projetado vem exclusivamente de `/cash-flow/projection`.
- Atrasados e próximos vêm exclusivamente de `/commitments/indicators`.
- Previsto não é somado a receitas/despesas realizadas.
- A seção de previsibilidade é consultiva; nenhum ledger é alterado.
- Relatórios históricos e exports existentes não são alterados nesta task; a análise detalhada permanece em Fluxo de Caixa/Agenda.
