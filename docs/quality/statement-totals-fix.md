# Correção dos totalizadores da fatura

## Correção

Após pagamento parcial ou total, o backend agora atualiza a fatura com:

```text
totalAmount       = valor original da fatura
paidAmount        = soma dos pagamentos ativos
outstandingAmount = totalAmount - paidAmount
```

Estados adicionados ao fluxo:

- `PARTIALLY_PAID` quando existe saldo residual;
- `PAID` quando o saldo chega a zero.

A interface já exibe os campos retornados pela API sem recalcular localmente.

## Validação

```bash
cd backend
./mvnw test -q
./mvnw package -q -DskipTests
```

Resultados: aprovados.
