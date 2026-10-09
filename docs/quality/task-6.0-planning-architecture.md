# TASK-6.0 — Arquitetura do Planejamento Financeiro

## Status

**CONCLUÍDA — arquitetura definida sem implementação funcional.**

## Decisões

- Orçamento MVP global por categoria e competência mensal.
- `EXPENSE` e `BOTH` elegíveis; `INCOME` não elegível.
- Realizado deriva de despesas `ACTIVE` por `occurredAt`.
- Transferências, receitas e canceladas não consomem orçamento.
- Unicidade: owner + categoria + ano + mês.
- Valores `DECIMAL(19,2)` e `BigDecimal`.
- Status derivado `ON_TRACK`, `WARNING`, `EXCEEDED`.
- Alertas in-app derivados.
- Metas usarão contribuições explícitas, sem alterar ledger ou saldo.
- Competência mensal usa intervalo UTC `[início, próximo início)`.
- Ownership sempre via `CurrentUser`.

## Documentos produzidos

- `docs/adr/0005-financial-planning-architecture.md`
- `docs/quality/task-6.0-planning-architecture.md`

## Contratos planejados

CRUD de orçamentos e resumo mensal em `/api/budgets`; metas e progresso serão definidos na TASK-6.6 após a modelagem de contribuições.

## Golden Scenario de orçamento

Orçamento Alimentação: R$ 1.000,00 em outubro/2026.

Despesas ativas: R$ 300,00 + R$ 450,00.
Despesa cancelada: R$ 200,00.
Receita BOTH: R$ 500,00.
Transferência: R$ 100,00.

Resultado esperado:

- Planned: R$ 1.000,00.
- Actual: R$ 750,00.
- Remaining: R$ 250,00.
- Consumption: 75%.

Com despesa ativa adicional de R$ 400,00:

- Actual: R$ 1.150,00.
- Remaining: -R$ 150,00.
- Consumption: 115%.
- Status: EXCEEDED.

## Plano de testes

CRUD, unicidade/concurrency, ownership, categorias inactive/BOTH, competência, cancelamentos, transferências, precisão, zero, >100%, resumo, alertas, metas/contribuições e regressões das Sprints 3–5 com MySQL Testcontainers.

## Ausência de implementação

Nenhuma entidade, migration, controller, service ou componente funcional da Sprint 6 foi criado nesta TASK.

## Roadmap

TASK-6.1 backend de orçamentos; TASK-6.2 frontend; TASK-6.3 realizado; TASK-6.4 dashboard; TASK-6.5 alertas; TASK-6.6 metas backend; TASK-6.7 metas frontend; TASK-6.8 gate.

READY FOR TASK-6.1 — Backend de Orçamentos por Categoria.
