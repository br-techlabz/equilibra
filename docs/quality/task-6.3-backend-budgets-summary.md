# TASK-6.3 — Backend de Planejado versus Realizado

## Status

**CONCLUÍDA — endpoint, agregação SQL, fórmulas e testes validados.**

## Endpoint

```http
GET /api/budgets/summary?month=YYYY-MM
```

## Regras

- Realizado deriva de despesas `ACTIVE` do ledger.
- `occurredAt` define a competência.
- Receitas e transferências não consomem orçamento.
- Canceladas não são contabilizadas.
- Categorias `BOTH` consideram somente despesas.
- Categorias inativas históricas permanecem consultáveis.
- Ownership é derivado de `CurrentUser`.
- Agregação ocorre em lote no MySQL.
- Não há persistência de `actualAmount`.

## Fórmulas

```text
remainingAmount = plannedAmount - actualAmount
consumptionPercentage = actualAmount / plannedAmount * 100
```

Orçamento zero retorna percentual nulo e status `ON_TRACK`. Percentuais acima de 100% são preservados.

## Golden scenario

Para planned `1000.00`, despesas válidas `300.00 + 450.00`, cancelada `200.00`, receita e transferência:

```text
plannedAmount = 1000.00
actualAmount = 750.00
remainingAmount = 250.00
consumptionPercentage = 75.00
```

## Testes executados

```bash
cd backend
./mvnw test -q
```

Resultado: aprovado, incluindo Testcontainers/MySQL e teste unitário das fórmulas.

## Fora do escopo

Frontend, gráficos, alertas, metas, exportação e alterações no ledger não foram implementados nesta task.
