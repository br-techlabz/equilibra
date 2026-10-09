# TASK-6.6 — Backend de Metas Financeiras

## Status

**CONCLUÍDA — backend compilado e suíte Testcontainers aprovada.**

## Modelo

- `FinancialGoal`: nome, descrição, targetAmount, targetDate, status e timestamps.
- `GoalContribution`: contribuição explícita de planejamento, sem alterar o ledger.
- Progresso deriva da soma das contribuições owner-scoped.
- Estados: `ACTIVE`, `COMPLETED`, `ARCHIVED`.

## Endpoints

```text
POST /api/goals
GET /api/goals
GET /api/goals/{id}
PUT /api/goals/{id}
POST /api/goals/{id}/contributions
POST /api/goals/{id}/complete
POST /api/goals/{id}/archive
```

## Segurança

- CurrentUser é a origem do owner.
- IDs estrangeiros retornam recurso inexistente.
- Contribuições são vinculadas à meta do mesmo usuário.
- Nenhum endpoint aceita ownerId.
- Metas não alteram receitas, despesas, transferências, saldos ou patrimônio.

## Testes

```bash
cd backend
./mvnw test -q
```

Resultado: aprovado com Testcontainers/MySQL.

Testes unitários adicionados para criação, valor inválido, conclusão e transições inválidas.

## Fora do escopo

Frontend, dashboard, gráficos, notificações, metas compartilhadas, integração bancária e alterações no ledger.
