# TASK-8.7 — Integração Financeira de Cartões

## Status

**CONCLUÍDA — integrações econômicas e de caixa implementadas e validadas.**

## Implementado

- Dashboard com `cardSummary`:
  - faturas/obrigações pendentes;
  - valor devido;
  - limite comprometido;
  - limite disponível.
- Orçamento por categoria inclui compras ativas de cartão pela competência da compra.
- Pagamentos `STATEMENT_PAYMENT` não entram no orçamento como nova despesa.
- Saldo, patrimônio e despesas bancárias continuam baseados no ledger.
- Compras de cartão permanecem separadas de saídas de caixa.
- Frontend mantém modelos compatíveis com `cardSummary`.

## Golden semântico

Para compra de R$200:

- antes do pagamento: saldo bancário não é reduzido, despesa econômica é R$200 e obrigação é R$200;
- após o pagamento: saldo reduz R$200, despesa econômica permanece R$200 e obrigação fica zero;
- nenhum KPI deve somar R$400 de despesa econômica.

## Validação

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
- frontend build: aprovado.

Warnings não bloqueantes permanecem em budgets de estilos e optional chaining preexistente.

## Limitações registradas

- Relatórios detalhados e exportações específicas de cartão permanecem dependentes da evolução dos DTOs de relatório;
- projeção detalhada de vencimentos de faturas depende da modelagem de compromissos futuros;
- nenhuma nova regra financeira foi criada no frontend.

**READY FOR TASK-8.8.**
