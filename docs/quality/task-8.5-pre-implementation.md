# TASK-8.5 — PRE-IMPLEMENTATION REPORT

## Status

**PREPARED — pronto para implementação após confirmação do serviço autoritativo de ledger.**

Nenhum código foi alterado nesta etapa.

## Contexto inspecionado

Foram inspecionados:

- `CLAUDE.md`;
- `docs/adr/0011-credit-card-architecture.md`;
- relatórios das TASKS 8.1–8.4;
- `FinancialTransaction`, `ExpenseService`, `TransferService` e repositories;
- `CreditCardStatement` e `CreditCardStatementService`;
- migrations V15–V20;
- regras de ownership, saldo derivado, cancelamento e concorrência;
- compromissos e projeção da Sprint 7;
- testes de integração e Testcontainers.

## Estado atual

`CreditCardStatement` possui total, pago e saldo devido, mas ainda não existe `StatementPayment`. A fatura atualmente fecha com `paidAmount=0` e `outstandingAmount=totalAmount`.

O ledger atual usa `FinancialTransaction` como fonte de verdade e `AccountBalanceQueryService` deriva saldo por transações ativas. `ExpenseService` cria despesa com saída de conta, enquanto `TransferService` representa movimentação entre contas. Não há ainda um serviço genérico separado de `AccountMovement`.

O pagamento de fatura precisa criar uma saída bancária real reutilizando a operação autoritativa disponível, sem transformar a fatura em despesa por categoria.

## Decisões de domínio

### `StatementPayment`

Criar entidade própria com:

- `id` UUID;
- `ownerId` imutável;
- `statementId`;
- `bankAccountId`;
- `amount` positivo `DECIMAL(19,2)`;
- `paidAt` como `Instant` efetivo;
- `status`: `ACTIVE`, `REVERSED`;
- `financialTransactionId` da única saída bancária;
- `idempotencyKey` opcional/owner-scoped;
- timestamps e `@Version`.

O pagamento não terá categoria econômica e não será criado como `Expense`.

### Política de pagamento

A ADR aprova pagamento total e parcial:

- valor deve ser positivo;
- valor não pode exceder `outstandingAmount`;
- pagamento parcial mantém saldo residual;
- pagamento que zera saldo leva fatura a `PAID`;
- fatura com saldo pendente pode ser marcada/derivada como `PARTIALLY_PAID` ou `OVERDUE`;
- excedente é rejeitado, sem crédito implícito;
- fatura `OPEN`, `PAID` ou `CANCELLED` não aceita pagamento;
- fatura `CLOSED` ou `OVERDUE` aceita pagamento dentro do saldo devido;
- conta pagadora precisa pertencer ao owner e estar ativa.

Como a TASK-8.4 ainda usa enum sem `PARTIALLY_PAID`/`PAID`, a TASK-8.5 deverá ampliar o enum e migration com nova migration incremental antes de persistir esses estados.

## Operação financeira autoritativa

O pagamento deve criar exatamente uma `FinancialTransaction` de saída bancária para a conta selecionada, com metadado/tipo que identifique `STATEMENT_PAYMENT`, sem categoria econômica de despesa.

Há uma decisão técnica essencial: o modelo atual de `FinancialTransaction` exige `category_id` e uma despesa exige categoria. Recomenda-se evoluir o ledger com um tipo explícito de movimento de pagamento de fatura, ou criar um serviço de movimentação que persista a saída sem classificá-la como `EXPENSE`, sem fazer o `AccountBalanceQueryService` ignorá-la.

Não reutilizar `ExpenseService.create`, pois isso duplicaria a despesa econômica no orçamento. Não criar `AssetAccount` para cartão.

A operação precisa ser uma transação única:

```text
lock statement + lock bank account
→ validar saldo/estado/ownership
→ criar uma saída bancária STATEMENT_PAYMENT
→ criar StatementPayment
→ atualizar paid/outstanding/status
→ commit
```

Qualquer falha deve reverter todos os passos.

## Idempotência, locking e concorrência

- Header `Idempotency-Key` recomendado e limitado a tamanho seguro;
- chave única por `(owner_id, idempotency_key)`;
- retry com mesma chave e mesmo pagamento retorna o pagamento existente;
- mesma chave com payload diferente retorna `409`;
- lock pessimista na fatura durante validação e atualização;
- lock/versão na conta para evitar saídas concorrentes acima do saldo;
- vínculo único `financial_transaction_id`;
- duas requisições simultâneas para o saldo residual produzem no máximo um efeito válido por saldo disponível;
- `@Transactional` no service.

Duplo clique e retry nunca podem criar segundo débito.

## Estorno/cancelamento

O pagamento possui histórico e não será excluído fisicamente. Um estorno futuro deverá:

- marcar `StatementPayment.REVERSED`;
- restaurar o valor pago/saldo devido da fatura;
- criar movimento corretivo inverso usando serviço autoritativo;
- não criar uma nova despesa por categoria;
- ser owner-scoped e idempotente.

Se o estorno não for implementado nesta task, o endpoint não deve ser exposto parcialmente; o modelo deve preservar os campos necessários para a task posterior.

## Contratos REST conceituais

```text
POST /credit-card-statements/{statementId}/payments
GET  /credit-card-statements/{statementId}/payments?page=&size=
```

Request:

```json
{
  "bankAccountId": "uuid",
  "amount": 200.00,
  "paidAt": "2026-10-11T15:30:00Z"
}
```

Header:

```text
Idempotency-Key: client-generated-key
```

Response deve conter:

- pagamento;
- fatura;
- valor pago e saldo residual;
- status atualizado;
- conta pagadora;
- id da transação bancária;
- data efetiva.

Não aceitar `ownerId`, categoria ou cartão como conta bancária no request.

## Integração com compromissos e projeção

Pagamento efetivo não deve liquidar uma `FinancialCommitment` de compra de cartão, porque compras não criam compromissos bancários individuais. Se existir compromisso materializado de vencimento de fatura em task futura, ele deverá referenciar exclusivamente a fatura e ser liquidado de forma idempotente pelo mesmo pagamento.

O pagamento realizado remove/atualiza o evento futuro da projeção, sem gerar novo compromisso nem duplicar saída.

## Migrations previstas

Confirmar maior versão no momento da implementação. Sugestão:

```text
V21__extend_statement_payment_status.sql
V22__create_statement_payments.sql
V23__extend_financial_transaction_for_statement_payment.sql
```

A estratégia exata depende da decisão sobre o ledger. Constraints mínimas:

- FK owner, statement e account;
- FK única para `financial_transaction_id`;
- `DECIMAL(19,2)` positivo;
- status válido;
- unique owner/idempotency key quando chave informada;
- índices `(owner_id, statement_id, paid_at)` e `(owner_id, bank_account_id, paid_at)`.

Não alterar migrations aplicadas V1–V20.

## Ownership e segurança

- owner exclusivamente por `CurrentUser.id()`;
- fatura e conta pagadora devem ser do mesmo owner;
- IDs de outro owner retornam `404`;
- conta inativa retorna conflito;
- não aceitar saldo, cartão ou owner enviados como fonte de autorização;
- Problem Details e Request ID existentes devem ser reutilizados;
- não registrar tokens, credenciais ou dados financeiros desnecessários.

## Testes previstos

### Domínio/service

- pagamento total de `R$ 200,00` quita fatura;
- conta de `R$ 1.000,00` passa a `R$ 800,00`;
- limite comprometido é liberado somente pelo pagamento;
- nenhuma despesa econômica adicional é criada;
- pagamento parcial preserva saldo residual;
- excedente é rejeitado;
- fatura aberta, paga ou cancelada é rejeitada;
- conta/cartão/fatura de outro owner retorna 404;
- data efetiva anterior ao vencimento é preservada;
- fatura vencida pode ser paga sem débito automático.

### Integração MySQL/Testcontainers

- migration e constraints;
- exatamente uma saída bancária;
- vínculo pagamento/transação;
- rollback se qualquer etapa falhar;
- retry idempotente;
- chave reutilizada com payload diferente retorna conflito;
- duas requisições concorrentes não duplicam débito;
- histórico de pagamento permanece após estorno/erro;
- orçamento não muda pela segunda vez;
- projeção/compromissos não duplicam liquidação;
- regressão de despesas, receitas, transferências e saldo.

## Riscos

- **Alto:** usar `ExpenseService` duplicaria despesa e orçamento. Mitigar com movimento `STATEMENT_PAYMENT` autoritativo.
- **Alto:** modelo atual exige categoria em `FinancialTransaction`; requer decisão de extensão do ledger antes de implementar.
- **Alto:** pagamento concorrente pode debitar duas vezes. Mitigar com locks, unique transaction e idempotência.
- **Médio:** estados de fatura atuais não incluem parcialmente paga/paga. Criar migration incremental.
- **Médio:** compromisso futuro pode ser liquidado duas vezes. Exigir vínculo exclusivo com statement.

## Decisão essencial / gate

Existe uma decisão técnica bloqueante antes da implementação: definir como `FinancialTransaction` representará uma saída `STATEMENT_PAYMENT` sem exigir categoria de despesa e sem fazer o pagamento aparecer como nova despesa econômica.

Recomendação: introduzir tipo explícito de movimento de pagamento no ledger e adaptar o cálculo de saldo para incluir a saída, mantendo relatórios de despesas por categoria fora desse tipo.

**READY TO IMPLEMENT após confirmar a extensão do ledger para `STATEMENT_PAYMENT`.**
