# TASK-8.6 — Frontend de Faturas e Pagamentos

## Status

**IMPLEMENTADA — interface integrada às APIs reais.**

## Implementado

- Modelos TypeScript de fatura, itens e pagamento.
- Consulta de faturas no detalhe do cartão.
- Exibição de ciclo, fechamento, vencimento, total, pago, devido e status.
- Exibição de itens/parcelas.
- Seleção de conta bancária real para pagamento.
- Confirmação explícita antes do envio.
- `Idempotency-Key` gerado por tentativa.
- Bloqueio de envio repetido durante operação.
- Atualização de faturas e cartões após pagamento.
- Fatura paga/cancelada sem botão de pagamento.
- Sem mutação local de saldo ou limite.
- Layout integrado à tela de cartões e ao Design System existente.

## Endpoints consumidos

```text
GET  /credit-cards/{cardId}/statements
POST /credit-card-statements/{statementId}/payments
GET  /asset-accounts
```

## Validação

```bash
cd frontend
npm run lint
npm test -- --watch=false
npm run build
```

Resultados:

- lint: aprovado;
- testes: 38 aprovados;
- build: aprovado.

Warnings não bloqueantes permanecem para budgets de estilos e optional chaining preexistente.

## Fora do escopo

- Dashboard;
- relatórios;
- novas regras financeiras;
- estorno de pagamentos.

**READY FOR TASK-8.7.**
