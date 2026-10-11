# TASK-8.3 — PRE-IMPLEMENTATION REPORT

## Status

**PREPARED — pronto para implementação após validação do contrato de integração.**

Nenhum código foi alterado nesta etapa.

## Contexto inspecionado

Foram inspecionados:

- `CLAUDE.md`;
- `docs/adr/0011-credit-card-architecture.md`;
- relatório da TASK-8.1;
- relatório/preparação da TASK-8.2;
- `FinancialTransaction` e `ExpenseService`;
- `CreateExpenseRequest` e `UpdateExpenseRequest`;
- `AssetAccount` e repositories owner-scoped;
- categorias e orçamento;
- compromissos, recorrências e projeção;
- formulário atual de despesas e seus serviços Angular;
- migrations Flyway e testes existentes.

## Achados principais

### Backend atual

`FinancialTransaction` representa o ledger bancário operacional e uma despesa convencional exige `sourceAccountId`. O `ExpenseService` valida uma `AssetAccount` ativa, cria a despesa e o `AccountBalanceQueryService` subtrai seu valor do saldo da conta.

Portanto, compra de cartão não pode reutilizar o fluxo bancário atual de forma direta. Se o cartão fosse enviado como `accountId` ou convertido em `AssetAccount`, a compra reduziria saldo bancário incorretamente.

O request atual é:

```text
CreateExpenseRequest:
- description
- occurredAt
- accountId
- amount
- categoryId
- notes
- tagIds
```

Será necessário introduzir uma origem discriminada sem quebrar despesas tradicionais.

### Frontend atual

`ExpensesPageComponent` carrega apenas `AssetAccount`, exibe um `mat-select` de contas e envia `accountId` ao `ExpensesApiService`. O serviço de cartões da TASK-8.1 já expõe a listagem owner-scoped de cartões.

O formulário atual também já possui:

- categorias aplicáveis a despesas;
- valor com duas casas;
- data/hora;
- tags;
- anexos;
- edição e cancelamento.

A integração deve preservar integralmente o caminho `BANK_ACCOUNT`.

## Decisões de domínio para implementação

### Origem discriminada

O novo contrato de despesa deve distinguir explicitamente:

```typescript
type ExpensePaymentSource =
  | { type: 'BANK_ACCOUNT'; accountId: string }
  | { type: 'CREDIT_CARD'; creditCardId: string; installmentCount: number };
```

No backend, recomenda-se um request de despesa compatível com o endpoint existente, mas com campos mutuamente exclusivos:

```json
{
  "description": "Compra",
  "occurredAt": "2026-10-11T12:00:00Z",
  "paymentMethod": "CREDIT_CARD",
  "accountId": null,
  "creditCardId": "uuid",
  "amount": 1200.00,
  "installmentCount": 3,
  "categoryId": "uuid",
  "notes": null,
  "tagIds": []
}
```

Para não quebrar clientes existentes, `paymentMethod` ausente poderá ser interpretado como `BANK_ACCOUNT` somente no contrato de transição, desde que `accountId` esteja presente. A implementação deverá documentar essa compatibilidade e validar que os dois meios não sejam enviados simultaneamente.

### Compra e parcelas

Criar:

```text
CreditCardPurchase
Installment
```

A compra deverá conter, no mínimo:

- `id` UUID;
- `ownerId` imutável;
- `creditCardId`;
- `categoryId`;
- descrição;
- valor total `BigDecimal`/`DECIMAL(19,2)`;
- `purchaseDate` civil `LocalDate` no timezone do cartão;
- quantidade de parcelas;
- status `ACTIVE`, `REVERSED` ou `CANCELLED`;
- timestamps e `@Version`;
- vínculo com o modelo econômico aprovado.

Cada parcela deverá conter:

- `id`;
- owner e cartão;
- compra;
- sequência e total;
- principal da parcela;
- ciclo/fatura de destino quando o modelo de fatura estiver disponível;
- estado derivado ou controlado conforme ADR.

Como faturas/pagamentos estão fora desta task, a associação inicial deverá usar um identificador de ciclo/fatura compatível com a próxima implementação, ou deixar a fatura como referência futura claramente definida. Não criar uma fatura completa paralela sem necessidade.

### Arredondamento

Calcular em centavos inteiros:

```text
base = total em centavos / número de parcelas
resto = total em centavos % número de parcelas
```

As primeiras `resto` parcelas recebem `base + 1`; as demais recebem `base`. A soma deve ser exatamente o valor total.

Exemplo:

```text
100,00 / 3
33,34 + 33,33 + 33,33 = 100,00
```

### Limite

A compra deve validar, na mesma transação:

```text
availableLimit = totalLimit - committedLimit - reservedLimit
```

O valor total da compra compromete o limite imediatamente, mesmo em parcelamento. Nesta task, `reservedLimit` permanece zero. O consumo deverá ser derivado de compras ativas, sem contador mutável fictício.

A consulta precisa ser ownership-aware e a verificação do limite precisa ser protegida contra compras concorrentes. Recomenda-se lock pessimista no cartão durante a validação e persistência ou outra estratégia equivalente comprovada por teste.

### Data de competência e ciclo

A compra usa `LocalDate` civil no timezone persistido do cartão. Regra do ADR:

- data anterior ao dia de fechamento: ciclo que fecha no mês corrente;
- data igual ou posterior ao dia de fechamento: ciclo do mês seguinte;
- compra exatamente no dia de fechamento: ciclo seguinte;
- dias 29, 30 e 31 inexistentes: último dia válido do mês.

A TASK-8.3 deve persistir informação suficiente para a TASK-8.4/8.5 gerar ou consultar faturas sem reinterpretar a compra.

## Efeito financeiro obrigatório

### Compra via conta bancária

Manter comportamento atual:

- cria `FinancialTransaction` `EXPENSE`;
- preenche `source_account_id`;
- reduz saldo derivado;
- entra no orçamento e nos relatórios existentes.

### Compra via cartão

- cria obrigação de cartão e parcelas;
- cria a despesa econômica por categoria no modelo aprovado;
- reduz limite disponível;
- não preenche `source_account_id` bancário;
- não cria saída bancária;
- não cria `FinancialTransaction` bancária fictícia;
- não altera saldo de `AssetAccount`;
- conta uma única vez no orçamento por categoria.

A decisão de persistir a despesa econômica em `FinancialTransaction` exige cuidado: a entidade atual exige uma conta de origem para `EXPENSE`. A recomendação é criar um modelo econômico de compra de cartão separado, integrado aos serviços de orçamento/relatórios por uma consulta específica, até que o ledger seja formalmente ampliado em ADR posterior. Não adulterar constraints do ledger para aceitar uma despesa sem origem sem decisão arquitetural explícita.

## Contratos REST previstos

Recomendação principal: manter `POST /expenses` como caso de uso de despesa e ampliar request com origem discriminada, preservando o endpoint convencional.

```text
POST /expenses
PUT /expenses/{id}
PATCH /expenses/{id}/cancel
```

Para operações específicas de cartão, expor adicionalmente:

```text
POST /credit-cards/{id}/purchases
GET  /credit-cards/{id}/purchases?page=&size=&status=
GET  /credit-card-purchases/{id}
POST /credit-card-purchases/{id}/reverse
```

A implementação deve escolher um contrato principal e não duplicar criação de compra em dois fluxos sem uma camada de aplicação comum. Requests nunca recebem `ownerId`; cartão e categoria são carregados por `id + CurrentUser.id()`.

Respostas devem ser DTOs e apresentar:

- identificação da compra;
- descrição, data, total e parcelas;
- cartão sem dados sensíveis;
- categoria;
- status;
- limite disponível retornado pela API quando apropriado;
- previsão do ciclo/fatura, sem simular pagamento.

## Persistência e migration prevista

Criar migrations Flyway novas, sem alterar as já aplicadas. Ordem sugerida, considerando que `V15` e `V16` já existem:

```text
V17__create_credit_card_purchases.sql
V18__create_credit_card_installments.sql
```

Caso a implementação use uma única migration, preservar constraints e separação lógica.

Constraints mínimas:

- FK owner → users;
- FK cartão → credit_cards;
- FK categoria → categories;
- UUIDs e owner imutáveis;
- valor total positivo `DECIMAL(19,2)`;
- parcela positiva `DECIMAL(19,2)`;
- quantidade e sequência de parcelas positivas;
- status válido;
- `UNIQUE(purchase_id, sequence_number)`;
- índices `(owner_id, credit_card_id, purchase_date)`;
- índices por status e cartão;
- vínculo idempotente se o request suportar chave externa.

Não criar FK ou coluna que transforme cartão em `AssetAccount`.

## Ownership e segurança

- owner exclusivamente de `CurrentUser.id()`;
- cartão, categoria e compra sempre carregados com owner;
- User A não pode referenciar cartão, categoria ou compra de User B;
- recurso de outro owner retorna `404` indistinguível;
- cartão inativo rejeita nova compra;
- categoria inativa ou incompatível rejeita nova compra;
- nenhum PAN, CVV, token ou credencial financeira trafega ou persiste;
- erros usam `ProblemDetail` e Request ID já existentes.

## Idempotência e concorrência

- criação de compra, parcelas e vínculo econômico na mesma transação;
- lock do cartão na verificação/consumo do limite;
- `@Version` em compra e cartão;
- retry não pode criar compra duplicada;
- se houver `Idempotency-Key`, criar constraint por owner/chave/operação;
- sem chave, a API deve documentar que retry de POST não é seguro até a proteção ser implementada;
- cancelamento/estorno deve ser terminal ou controlado por transição explícita;
- compra já faturada ou paga não pode ser editada silenciosamente.

## Frontend previsto

Modificar:

```text
frontend/src/app/features/expenses/pages/expenses.page.ts
frontend/src/app/features/expenses/pages/expenses.page.scss
frontend/src/app/features/expenses/models/expense.models.ts
frontend/src/app/features/expenses/data-access/expenses-api.service.ts
```

Reutilizar:

```text
frontend/src/app/features/asset-accounts/data-access/asset-accounts-api.service.ts
frontend/src/app/features/credit-cards/data-access/credit-cards-api.service.ts
frontend/src/app/features/categories/data-access/categories-api.service.ts
```

Adicionar ao formulário:

- seleção de meio de pagamento (`Conta bancária`/`Cartão de crédito`);
- seleção condicional de conta ou cartão;
- quantidade de parcelas para cartão;
- preview real da previsão do ciclo, sem calcular limite localmente;
- aviso: “Esta compra será cobrada na fatura e não reduzirá o saldo bancário agora.”

A seleção deve usar tipo discriminado, nunca um UUID único sem contexto. O fluxo bancário atual deve permanecer visual e funcionalmente compatível.

## Testes previstos

### Domínio/backend

- compra à vista válida;
- compra parcelada válida;
- valor positivo e no máximo duas casas;
- quantidade de parcelas válida e limitada por política;
- parcelas somam exatamente o total;
- cartão ativo aceito;
- cartão inativo rejeitado;
- categoria EXPENSE/BOTH aceita;
- categoria INCOME/inativa rejeitada;
- limite suficiente aceito;
- limite insuficiente rejeitado;
- compra não altera `AssetAccount`;
- compra não cria `FinancialTransaction` bancária;
- User A não acessa cartão de B;
- data no fechamento vai para ciclo seguinte;
- dias 29/30/31 normalizados;
- retry/concurrency não duplica compra;
- cancelamento e estorno preservam histórico e liberam limite conforme ADR;
- despesa bancária tradicional continua igual.

### Integração MySQL/Testcontainers

- migrations V17/V18;
- FKs e checks;
- queries owner-scoped;
- lock/concorrência;
- rollback completo se parcela ou vínculo falhar;
- orçamento não duplica a compra;
- saldo bancário permanece intacto.

### Frontend

- contas e cartões aparecem como meios distintos;
- `BANK_ACCOUNT` envia `accountId` e preserva request anterior;
- `CREDIT_CARD` envia `creditCardId` e parcelas;
- nunca envia ownerId;
- aviso de não redução do saldo aparece apenas para cartão;
- validação de valor e parcelas;
- resposta `400`, `404`, `409` e Problem Details;
- loading, erro e retry;
- edição/cancelamento conforme estado;
- mobile, teclado e contraste;
- lint, unit tests, build e E2E.

## Golden scenarios

1. Conta A = `R$ 1.000,00`, cartão limite = `R$ 5.000,00`, compra cartão = `R$ 200,00`:
   - conta permanece `R$ 1.000,00`;
   - obrigação = `R$ 200,00`;
   - disponível = `R$ 4.800,00`;
   - nenhuma saída bancária.
2. Compra `R$ 1.200,00` em 3 parcelas:
   - parcelas somam exatamente `R$ 1.200,00`;
   - total compromete o limite;
   - parcelas são distribuídas pelos ciclos corretos.
3. Compra exatamente no fechamento pertence ao ciclo seguinte.
4. Compra cancelada/estornada não cria movimento fantasma.
5. Compra bancária convencional continua reduzindo a conta.
6. User A não consulta nem altera compra/cartão de User B.

## Riscos e decisões

- **Alto — modelo econômico:** `FinancialTransaction.EXPENSE` atual exige conta bancária; não deve ser forçado a representar cartão sem alteração arquitetural explícita. A implementação deve escolher uma entidade econômica de cartão ou produzir ADR complementar.
- **Alto — dupla contagem:** compra por cartão não pode aparecer como despesa econômica duplicada por parcela ou pagamento futuro.
- **Alto — limite concorrente:** lock e transação são obrigatórios.
- **Médio — fatura ainda fora do escopo:** persistir ciclo de forma compatível, sem criar pagamento ou fatura operacional prematura.
- **Médio — contrato atual:** ampliar `CreateExpenseRequest` exige compatibilidade com clientes existentes e testes de regressão.
- **Baixo — preview de fatura:** frontend deve mostrar somente previsão retornada pelo backend, não simular regras financeiras.

## Decisão essencial / gate

Há uma decisão arquitetural que precisa ser confirmada antes de codificar o backend: como a “despesa econômica” da compra de cartão será integrada ao orçamento/relatórios enquanto `FinancialTransaction.EXPENSE` exige `source_account_id` bancário.

Recomendação: criar `CreditCardPurchase` como fonte econômica própria e adaptar consultas de orçamento/relatórios somente onde necessário, sem inserir movimento no ledger bancário. Se essa decisão for aceita, o trabalho está pronto para implementação.

**READY TO IMPLEMENT TASK-8.3 — condicionado à confirmação do modelo econômico acima.**
