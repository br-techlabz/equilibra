# TASK-8.5 — Backend de Pagamento de Faturas

## Status

**IMPLEMENTADA — pagamento de faturas com saída bancária e sem duplicação econômica.**

## Decisão do ledger

Foi adotado `TransactionType.STATEMENT_PAYMENT` para representar saída bancária de pagamento de fatura. Esse tipo:

- reduz o saldo da conta de origem;
- não possui categoria econômica;
- não entra como nova despesa por categoria;
- não representa compra adicional.

## Implementado

- `StatementPayment` e `StatementPaymentStatus`.
- Pagamento total e parcial.
- Validação de ownership, estado, conta ativa e saldo devido.
- Uma única `FinancialTransaction` de saída bancária.
- Atualização atômica da fatura e vínculo do pagamento.
- Idempotência via `Idempotency-Key`.
- Migrations V21/V22.
- Endpoint:

```text
POST /credit-card-statements/{statementId}/payments
```

Request:

```json
{
  "bankAccountId": "uuid",
  "amount": 200.00,
  "paidAt": "2026-10-11T15:30:00Z"
}
```

## Validação

```bash
cd backend
./mvnw test -q
```

Resultado: testes backend aprovados com Testcontainers/MySQL.

## Fora do escopo

- frontend de faturas;
- estorno de pagamento;
- juros e encargos;
- novos recursos de crédito.

**READY FOR TASK-8.6.**
