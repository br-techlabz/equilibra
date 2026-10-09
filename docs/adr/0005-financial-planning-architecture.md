# ADR 0005 — Arquitetura do Planejamento Financeiro

## Status

Aceita para orientar a Sprint 6. Esta decisão define arquitetura e contratos; não implementa código funcional, migrations ou endpoints.

## Contexto

A Sprint 5 consolidou relatórios derivados de `FinancialTransaction + AssetAccount.initialBalance`. A Sprint 6 adicionará planejamento sem criar uma segunda fonte de verdade financeira. Orçamentos e metas são projeções de planejamento e nunca alteram o ledger.

## Decisões

### Orçamentos

O MVP usará `CategoryBudget`, orçamento global por categoria e competência mensal:

```text
id UUID
ownerId UUID
categoryId UUID
year SMALLINT
month TINYINT
plannedAmount DECIMAL(19,2)
createdAt Instant
updatedAt Instant
```

A identidade `(ownerId, categoryId, year, month)` é imutável e possui constraint única. Apenas `plannedAmount` pode ser editado; para trocar categoria/mês, cria-se outro orçamento.

`plannedAmount` deve ser >= 0, com no máximo duas casas; zero é permitido e significa orçamento sem limite monetário definido, com consumo percentual não calculado (`null`) e status `ON_TRACK`.

### Categorias

- `EXPENSE`: elegível.
- `BOTH`: elegível; somente transações `EXPENSE` consomem.
- `INCOME`: não elegível.
- Categoria inativa não aceita novos orçamentos, mas orçamentos e histórico existentes permanecem consultáveis.

### Competência

A competência é `year + month`, sem data artificial persistida. O mês é convertido para intervalo UTC semiaberto:

```text
[first day at 00:00 UTC, first day of next month at 00:00 UTC)
```

A apuração usa `occurredAt`, nunca `createdAt`.

### Realizado e indicadores

`actualAmount` é sempre calculado a partir de despesas `ACTIVE` da categoria no intervalo mensal. Cancelamentos não contribuem; transferências e receitas não contribuem.

```text
remainingAmount = plannedAmount - actualAmount
consumptionPercentage = actualAmount / plannedAmount * 100, quando plannedAmount > 0
```

O percentual não é limitado a 100%. Status derivado:

- `ON_TRACK`: < 80% ou orçamento zero.
- `WARNING`: >= 80% e <= 100%.
- `EXCEEDED`: > 100%.

Alertas MVP são in-app e derivados, não persistidos; não haverá e-mail/push nesta Sprint.

### Metas

A Sprint 6 adotará contribuições explícitas como registros de planejamento, sem lançar `Income`, `Expense` ou `Transfer` e sem alterar saldos. O modelo será definido na TASK-6.6:

```text
FinancialGoal(id, ownerId, name, targetAmount, targetDate, status, createdAt, updatedAt)
GoalContribution(id, ownerId, goalId, amount, occurredAt, note, createdAt)
```

`currentAmount` é a soma das contribuições ativas owner-scoped. Não será inferido automaticamente de saldo de conta. Status da meta é independente de `TransactionStatus` e terá `ACTIVE`, `COMPLETED`, `ARCHIVED`.

## Contratos REST planejados

Orçamentos:

```text
POST   /api/budgets
GET    /api/budgets?year=&month=&categoryId=&page=&size=
GET    /api/budgets/{id}
PUT    /api/budgets/{id}
DELETE /api/budgets/{id}
GET    /api/budgets/summary?year=&month=
```

O resumo retorna total planejado, total realizado, saldo disponível, categorias em alerta/excedidas e breakdown por categoria. Todos os cálculos são backend.

Metas, após confirmação da modelagem na TASK-6.6:

```text
POST /api/goals
GET /api/goals
GET /api/goals/{id}
PUT /api/goals/{id}
POST /api/goals/{id}/complete
POST /api/goals/{id}/archive
GET /api/goals/{id}/progress
```

Requests nunca recebem `ownerId`; IDs estrangeiros são tratados como inexistentes conforme política privada atual. Erros usam Problem Details e Request ID.

## Persistência planejada

Migration Flyway futura criará `category_budgets` com UUID, FK para categoria, owner_id, `year`, `month`, `DECIMAL(19,2)`, timestamps, índice por `(owner_id, year, month)` e constraint única `(owner_id, category_id, year, month)`. Migrations aplicadas não serão alteradas.

Metas/contribuições terão migrations próprias na TASK-6.6. Não materializar `actualAmount` como verdade mutável.

## Segurança e performance

- CurrentUser é a única origem do owner.
- Repositories são owner-aware.
- Categoria deve pertencer ao owner e ter applicability elegível.
- Consultas mensais devem agregar no banco, evitar N+1 e joins multiplicadores.
- MySQL/Testcontainers são obrigatórios para integração.
- BigDecimal permanece em toda a cadeia.

## Roadmap

- TASK-6.0: ADR e arquitetura.
- TASK-6.1: backend CRUD de orçamentos.
- TASK-6.2: interface de orçamentos.
- TASK-6.3: planejado versus realizado.
- TASK-6.4: dashboard de orçamentos.
- TASK-6.5: alertas in-app.
- TASK-6.6: backend de metas/contribuições.
- TASK-6.7: interface de metas.
- TASK-6.8: gate.

## Fora do escopo

XLSX, notificações externas, agendamento, compartilhamento, assinatura digital, event sourcing, alteração do ledger, saldo reservado automático e implementação funcional nesta TASK.
