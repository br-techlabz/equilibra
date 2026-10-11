# TASK-8.3 — Compras e Parcelamentos no Cartão

## Status

**CONCLUÍDA — funcionalidade implementada, listagem integrada e validações executadas.**

## Entregas

- Compras à vista e parceladas no cartão.
- Parcelas com arredondamento exato em centavos.
- Validação de cartão, categoria, limite e ownership.
- Lock pessimista durante reserva de limite.
- Cancelamento de compra pelo fluxo específico de cartão.
- Nenhuma transação bancária fictícia para compra no cartão.
- Despesas bancárias tradicionais preservadas.
- Formulário de despesas com Conta bancária, Dinheiro e Cartão de crédito.
- Compras de cartão exibidas na listagem unificada de despesas.
- Origem, quantidade de parcelas e ciclo retornados no contrato econômico.
- Filtro de canceladas respeitado.
- Ação de edição ocultada para compras de cartão; cancelamento usa endpoint próprio.
- Ownership mantido em todas as consultas.

## Endpoints

```text
POST /credit-card-purchases
GET  /credit-card-purchases/card/{cardId}
GET  /credit-card-purchases/{id}
POST /credit-card-purchases/{id}/cancel
GET  /expenses
```

## Migrations

```text
V17__create_credit_card_purchases.sql
V18__create_credit_card_installments.sql
```

## Validações executadas

Backend:

```bash
cd backend
./mvnw test -q
./mvnw package -q -DskipTests
```

Frontend:

```bash
cd frontend
npm run lint
npm test -- --watch=false
npm run build
```

Resultados:

- backend tests: aprovados;
- backend package: aprovado;
- frontend lint: aprovado;
- frontend tests: 38 aprovados;
- frontend build: aprovado;
- Docker build/publicação: executado.

Warnings de budget de estilos e optional chaining permanecem preexistentes/não bloqueantes.

## Cenários confirmados pelo contrato

- Compra cartão aparece na listagem de despesas após reload.
- Compra cartão é identificada como `CREDIT_CARD` e não como conta indisponível.
- Compra cancelada desaparece da lista padrão e aparece com `includeCancelled=true`.
- User A não consulta compra de User B.
- Despesa bancária continua no ledger tradicional.
- Compra cartão não reduz saldo de `AssetAccount`.
- Compra cartão não cria `FinancialTransaction` bancária.
- Cancelamento usa endpoint de compra, sem ação de edição indevida.

## Limitações remanescentes explicitamente fora do gate

- Idempotência por `Idempotency-Key` persistente ainda deve ser adicionada em uma evolução de robustez de API; a concorrência de limite está protegida por lock.
- A integração final com pagamentos de fatura, dashboards e relatórios financeiros permanece nas tasks posteriores previstas pelo ADR.
- Não foi criada uma nova suíte E2E exclusiva para cartão nesta execução; a suíte unitária e os builds foram executados.

Essas limitações não impedem o avanço da implementação prevista para a TASK-8.4.

**READY FOR TASK-8.4.**
