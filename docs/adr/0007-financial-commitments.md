# ADR 0007 — Compromissos Financeiros e Efetivação

## Status

Aceito para a TASK-7.3.

## Decisões

- `FinancialCommitment` representa uma previsão individual; `RecurrenceRule` representa a série; `FinancialTransaction` representa o realizado.
- Compromissos manuais são permitidos e não possuem `recurrenceRuleId`.
- Geração é sob demanda, sem scheduler obrigatório, limitada a intervalo máximo de 12 meses.
- Estados persistidos: `PENDING`, `SETTLED`, `CANCELLED`; vencido é derivado.
- Efetivação exige `occurredAt` e valor explícitos; não altera `plannedAmount`.
- Efetivação reutiliza Expense/Income e ocorre atomicamente com vínculo único à transação.
- Segunda efetivação retorna conflito; locking protege concorrência.
- Conta/categoria inativa rejeita novas previsões e efetivação histórica.
- Pagamentos parciais, estorno, calendário e frontend ficam fora do escopo.
