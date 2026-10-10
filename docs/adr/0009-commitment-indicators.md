# ADR 0009 — Indicadores de Vencimentos

## Status

Aceito para TASK-7.6.

## Decisões

- Indicadores são derivados e read-only; não são persistidos.
- Apenas compromissos `PENDING` são elegíveis.
- `dueDate < referenceDate`: atrasado.
- `dueDate == referenceDate`: vence hoje.
- `referenceDate + 1` até `+7`: próximo.
- Após a janela de 7 dias: pendente futuro.
- `SETTLED` e `CANCELLED` não entram em pendências.
- Valores de despesas e receitas são separados e calculados com BigDecimal.
- Data de referência é civil e pode ser informada pelo cliente; o default é a data atual da aplicação.
- Ownership é derivado de `CurrentUser`.
