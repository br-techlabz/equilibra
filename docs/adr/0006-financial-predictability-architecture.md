# ADR 0006 — Arquitetura de Previsibilidade Financeira

## Status

Aceita para orientar a Sprint 7. Não implementa código, migrations ou endpoints.

## Decisões

- `FinancialTransaction` continua a única fonte de verdade de realizado, saldo e patrimônio.
- Previsões não alteram ledger, saldos, orçamento consumido ou relatórios realizados.
- O domínio separa `RecurringPlan` (série) de `FinancialCommitment` (ocorrência).
- Efetivação explícita cria exatamente uma transação financeira idempotente, com chave de efetivação por ocorrência.
- MVP suporta periodicidade mensal e anual; semanal e customizada ficam adiadas.
- Dia inexistente usa o último dia válido do mês (31 de fevereiro vira 28/29).
- Ocorrências usam `FORECAST`, `PENDING`, `PAID`, `RECEIVED`, `OVERDUE`, `CANCELLED` conforme tipo; transições inválidas são rejeitadas.
- Pagamentos parciais ficam fora do MVP.
- Edição isolada de ocorrência não altera a série; edição da série afeta somente ocorrências futuras não editadas.
- Cancelar ocorrência não cancela a série; cancelar série impede novas ocorrências.
- Vencimento civil usa `LocalDate` e timezone explícito; efetivação usa `Instant`/`occurredAt`.
- Tudo é owner-scoped por `CurrentUser`; nunca aceitar `ownerId`.
- “Contas de Despesas” permanece `PENDING PRODUCT DECISION`; não criar novo tipo de conta nesta Sprint.

## Modelo conceitual

```text
RecurringPlan
  id, ownerId, kind(EXPENSE|INCOME), name, description,
  accountId, categoryId, amount DECIMAL(19,2),
  startDate, endDate?, dayOfMonth, frequency(MONTHLY|YEARLY), status

FinancialCommitment
  id, ownerId, recurringPlanId, dueDate, amount,
  status, effectiveTransactionId?, createdAt, updatedAt
```

Nenhum desses conceitos substitui `FinancialTransaction`.

## Estados

```text
FORECAST -> PENDING -> PAID/RECEIVED
FORECAST -> CANCELLED
PENDING  -> OVERDUE -> PAID/RECEIVED
RecurringPlan ACTIVE -> CANCELLED/ARCHIVED
```

A geração deve ser determinística e idempotente por `(ownerId, planId, dueDate)`.

## Impactos

- Ledger: somente efetivação explícita cria transação.
- Orçamentos: somente a transação efetivada consome orçamento.
- Relatórios: previsões não aparecem em realizados; projeções usam modelo separado.
- Dashboard: saldo atual não muda; saldo projetado será explicitamente identificado.
- Categorias/contas inativas: não criar novas previsões inválidas; histórico permanece.

## Contratos conceituais futuros

```text
POST/GET/PUT /api/recurring-plans
POST /api/recurring-plans/{id}/cancel
GET /api/commitments?from=&to=&status=
POST /api/commitments/{id}/effectuate
POST /api/commitments/{id}/cancel
GET /api/calendar?from=&to=
GET /api/cash-flow/projection?from=&to=
```

Contratos definitivos serão aprovados nas TASKS 7.1–7.3.

## Roadmap

- 7.1 Backend de recorrências e geração idempotente.
- 7.2 Interface de recorrências.
- 7.3 Compromissos, vencimentos e efetivação.
- 7.4 Calendário financeiro.
- 7.5 Projeção de fluxo de caixa.
- 7.6 Indicadores de atraso.
- 7.7 Integração com Dashboard/Relatórios.
- 7.8 Gate da Sprint 7.

## Fora do escopo

Pagamento parcial, contas de despesas, notificações externas, integração bancária, scheduler obrigatório, alterações automáticas no ledger, metas e funcionalidades da Sprint 8.
