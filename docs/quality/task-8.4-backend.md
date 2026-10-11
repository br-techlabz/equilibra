# TASK-8.4 — Backend de Faturas e Fechamento

## Status

**IMPLEMENTADA — backend de faturas, associação de parcelas e fechamento disponível.**

## Implementado

- `CreditCardStatement` e `CreditCardStatementStatus`.
- Estados `OPEN`, `CLOSED`, `OVERDUE` e `CANCELLED`.
- Migrations V19/V20.
- Associação opcional `statement_id` nas parcelas.
- Unique cycle por owner/cartão/período.
- Cálculo de total, pago e saldo devido com `BigDecimal`.
- Fechamento transacional e owner-scoped.
- Lock de ciclo e fechamento repetível.
- Associação de parcelas por cartão e mês de ciclo.
- Vencimento normalizado para meses curtos.
- Endpoints:
  - `GET /credit-cards/{cardId}/statements`;
  - `GET /credit-card-statements/{id}`;
  - `POST /credit-cards/{cardId}/statements/close`.
- Nenhum pagamento implementado.
- Nenhuma conta bancária, saldo ou `FinancialTransaction` alterada.
- Fechamento não libera limite.

## Validação

```bash
cd backend
./mvnw test -q
```

Resultado: testes backend aprovados com Testcontainers/MySQL.

## Fora do escopo

- pagamentos;
- juros e encargos;
- frontend de faturas;
- projeção final de pagamentos.

**READY FOR TASK-8.5.**
