# VISUALIZAÇÃO DA FATURA ATUAL — RELATÓRIO FINAL

## STATUS: CONCLUÍDA

## Correções e melhorias

- Fatura atual derivada por ciclo real do cartão.
- Lançamentos exibidos com descrição original cadastrada na compra, não UUID.
- Resumo visual com total, pago, devido, fechamento e vencimento.
- Parcelas mostram sequência, ciclo e valor.
- Consulta read-only sem alteração de saldo, limite ou ledger.

## Validação

```bash
cd backend
./mvnw test -q
cd ../frontend
npm run lint
npm run build
```

Resultados: backend, lint e build aprovados.
