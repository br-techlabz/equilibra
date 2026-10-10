# TASK-7.0 — ARCHITECTURE REPORT

## Status

**COMPLETED — arquitetura documentada sem implementação funcional.**

## Entregável principal

```text
docs/adr/0006-financial-predictability-architecture.md
```

## Decisões principais

- Previsão é distinta de lançamento efetivado.
- Previsões não alteram saldo, patrimônio, ledger, orçamento ou relatórios realizados.
- Série recorrente e ocorrência futura são modelos distintos.
- Efetivação explícita é idempotente e cria uma única `FinancialTransaction`.
- MVP mensal/anual; semanal/customizada adiadas.
- Datas civis usam `LocalDate`; ledger efetivado usa `Instant`.
- Dias inexistentes usam o último dia válido do mês.
- Pagamentos parciais e Contas de Despesas estão fora do MVP; Contas de Despesas é decisão de produto pendente.
- Ownership usa CurrentUser e todas as consultas futuras serão owner-scoped.

## Golden scenarios

1. Previsão de despesa não altera saldo ou relatório realizado.
2. Efetivação de uma ocorrência cria somente uma transação.
3. Repetição da efetivação não duplica a transação.
4. Ocorrência vencida fica `OVERDUE` sem alterar o ledger.
5. Dia 31 em fevereiro usa o último dia válido.
6. Cancelamento de ocorrência não cancela a série.
7. Cancelamento de série não cria novas ocorrências.
8. User A nunca consulta ou efetiva dados de User B.

## Decisões pendentes

- Produto deve definir o conceito de “Contas de Despesas” antes de qualquer implementação relacionada.
- Contratos definitivos e estratégia de geração sob demanda/scheduler serão detalhados nas TASKS 7.1–7.3.

## Roadmap

TASK-7.1 recorrências; TASK-7.2 interface; TASK-7.3 compromissos/efetivação; TASK-7.4 calendário; TASK-7.5 projeção; TASK-7.6 atrasos; TASK-7.7 integrações; TASK-7.8 gate.

## Confirmação

Nenhum código Java, TypeScript, migration ou endpoint funcional foi implementado na TASK-7.0.

READY FOR TASK-7.1 — Backend de Recorrências.
