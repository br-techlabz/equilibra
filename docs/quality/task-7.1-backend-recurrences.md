# TASK-7.1 — Backend de Recorrências

## Status

**CONCLUÍDA — CRUD owner-scoped, estados, validações e migration implementados.**

## Implementado

- `RecurrenceRule` com tipos EXPENSE/INCOME.
- Periodicidades MONTHLY/YEARLY.
- Datas de início/fim e dia de vencimento.
- Valor DECIMAL(19,2).
- Estados ACTIVE, PAUSED, ENDED e CANCELLED.
- CRUD REST e transições pause/resume/end/cancel.
- Categorias e contas owner-scoped e ativas.
- Recorrências não criam transações nem alteram saldos.
- Migration V12 com FKs, checks e índices.

## Testes

```bash
cd backend
./mvnw test -q
```

Resultado: aprovado com Testcontainers/MySQL.

Teste unitário adicionado para criação, valor inválido e transições.

## Fora do escopo

Geração de ocorrências, efetivação financeira, frontend, calendário, projeção de fluxo de caixa e notificações.

READY FOR TASK-7.2 — Frontend de Recorrências.
