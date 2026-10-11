# Integração de compras de cartão na listagem de despesas

## Implementado

- `GET /expenses` agora combina despesas bancárias e compras de cartão do usuário autenticado.
- Resposta `ExpenseResponse` ganhou origem discriminada:
  - `paymentMethod`;
  - `creditCardId`;
  - `installmentCount`;
  - `cycleMonth`.
- Compras de cartão aparecem como despesas econômicas, sem `accountId` bancário.
- Despesas bancárias continuam com `paymentMethod=BANK_ACCOUNT`.
- Compras canceladas respeitam `includeCancelled`.
- Ordenação combinada por data e paginação server-side após a união.
- Frontend exibe “Cartão de crédito” como origem, em vez de “Conta indisponível”.
- Nenhuma alteração foi feita no cálculo de saldo bancário.

## Validação

Backend:

```bash
cd backend
./mvnw test -q
./mvnw package -q -DskipTests
```

Frontend:

```bash
npm run lint
npm run build
```

Resultados:

- testes backend: aprovados;
- package backend: aprovado;
- lint frontend: aprovado;
- build frontend: aprovado.

O build frontend mantém warnings de budget de estilos e um warning preexistente de optional chaining; não houve falha de compilação.

## Comportamento esperado

Após cadastrar uma compra no cartão e recarregar a tela de despesas, ela aparece na mesma listagem das despesas bancárias, com:

- descrição;
- data;
- valor;
- categoria;
- origem “Cartão de crédito”;
- quantidade de parcelas quando disponível.

A compra não aparece como movimento bancário e não reduz o saldo da conta.
