# TASK-8.4 — PRE-IMPLEMENTATION REPORT

## Status

**PREPARED — pronto para implementação após revisão do modelo de ciclos.**

Nenhum código foi alterado nesta etapa.

## Contexto inspecionado

Foram inspecionados:

- `CLAUDE.md`;
- `docs/adr/0011-credit-card-architecture.md`;
- código das TASKS 8.1–8.3;
- `CreditCard`, `CreditCardPurchase`, `Installment` e seus repositories/services/controllers;
- migrations V15–V18;
- regras de ownership, limites e saldo;
- integração da compra com a listagem de despesas;
- `FinancialTransaction`, `ExpenseService`, compromissos e projeção.

## Estado atual encontrado

A TASK-8.3 já persiste:

- `CreditCardPurchase` com cartão, categoria, valor total, data, quantidade e `cycleMonth`;
- `Installment` com sequência, valor principal e `cycleMonth`;
- status de compra `ACTIVE`, `REVERSED`, `CANCELLED`.

A associação atual ainda não possui `statementId`. O campo `cycleMonth` é a referência temporal disponível para a futura fatura.

O serviço atual calcula o ciclo assim:

```text
purchaseDate.dayOfMonth < closingDay
    -> primeiro dia do mês da compra
caso contrário
    -> primeiro dia do mês seguinte
```

A compra exatamente no dia do fechamento vai para o ciclo seguinte, conforme o ADR. As parcelas atualmente incrementam o mês a partir do ciclo base.

Ainda não existem:

- `CreditCardStatement`;
- estados persistidos de fatura;
- total/pago/saldo devido;
- associação persistida parcela → fatura;
- endpoints de fatura;
- fechamento transacional;
- geração idempotente de faturas;
- índice/constraint de ciclo por cartão.

## Decisões de domínio

### Entidade `CreditCardStatement`

Criar agregado owner-scoped com:

- `id` UUID;
- `ownerId` imutável;
- `creditCardId`;
- `cycleStart` e `cycleEnd` como `LocalDate` civis;
- `closingAt` como `Instant` derivado no timezone do cartão;
- `dueDate` como `LocalDate` normalizada;
- `status`;
- `totalAmount` `DECIMAL(19,2)`;
- `paidAmount` inicialmente `0.00`;
- `outstandingAmount` derivado/persistido com consistência transacional;
- `closedAt` opcional;
- timestamps `Instant`;
- `@Version`.

Estados mínimos para esta task:

```text
OPEN
CLOSED
OVERDUE
CANCELLED
```

`PARTIALLY_PAID` e `PAID` pertencem semanticamente ao fluxo de pagamentos, que está fora da TASK-8.4. Podem ser reservados no enum/migration para compatibilidade com o ADR, mas não haverá operação de pagamento nesta task. `OVERDUE` pode ser derivado de vencimento passado e saldo pendente; não deve criar débito.

### Associação de parcela

Adicionar `statement_id` nullable em `credit_card_installments` por migration incremental. Uma parcela ativa só poderá ser associada a uma fatura do mesmo cartão e owner.

A associação deve ser idempotente:

- mesma parcela nunca aparece duas vezes na mesma fatura;
- fechamento repetido não duplica itens;
- constraint/lock protege geração concorrente.

## Regras de ciclo e datas

Para cartão com `closingDay`:

- compra com dia menor que o fechamento pertence ao ciclo que fecha no mês da compra;
- compra no dia igual ou posterior ao fechamento pertence ao ciclo seguinte;
- compra exatamente no fechamento pertence ao ciclo seguinte;
- `cycleStart` é o dia posterior ao fechamento anterior;
- `cycleEnd` é o dia do fechamento inclusivo;
- consultas usam intervalo técnico `[cycleStart, nextCycleStart)`;
- vencimento é calculado no mês do fechamento mais a política de vencimento do cartão, com normalização para o último dia válido quando necessário;
- dias 29/30/31 em mês curto usam o último dia do mês;
- fevereiro bissexto usa `YearMonth.lengthOfMonth()`;
- timezone do cartão, não timezone do servidor, decide `closingAt`.

A lógica de ciclo deve ser centralizada em um componente de domínio/serviço testável, não duplicada em controllers ou repositories.

## Fechamento

Caso de uso recomendado:

```text
closeStatement(cardId, cycleStart/cycleMonth)
```

Comportamento:

1. carregar cartão por `id + ownerId`;
2. validar cartão e ciclo;
3. obter/criar fatura `OPEN` com lock;
4. selecionar parcelas elegíveis do mesmo cartão/owner/ciclo;
5. associar parcelas sem `statement_id`;
6. somar parcelas ativas com `BigDecimal`;
7. calcular vencimento;
8. alterar para `CLOSED` e preencher `closedAt`;
9. repetir a operação retorna a mesma fatura sem duplicar itens.

Uma fatura fechada não será reaberta nem recalculada silenciosamente. Correções futuras usarão estorno/ajuste próprio.

A consulta de uma fatura aberta pode materializar/derivar a fatura conforme a escolha da implementação, mas não deve criar duplicatas. Recomenda-se persistir a fatura no primeiro fechamento e permitir consulta determinística de ciclos.

## Limite e saldo bancário

O fechamento não libera limite. O limite continua derivado de compras ativas não liquidadas:

```text
availableLimit = totalLimit - committedLimit - reservedLimit
```

Nesta task não existe pagamento e, portanto:

- `paidAmount = 0.00` para faturas novas;
- `outstandingAmount = totalAmount`;
- nenhuma `AssetAccount` é alterada;
- nenhuma `FinancialTransaction` é criada;
- nenhum compromisso bancário é criado automaticamente.

Vencimento passado apenas muda/deriva o estado `OVERDUE`.

## Contratos REST conceituais

```text
GET  /credit-cards/{cardId}/statements?page=&size=&status=
GET  /credit-card-statements/{id}
GET  /credit-card-statements/{id}/items?page=&size=
POST /credit-cards/{cardId}/statements/close
```

O contrato de fechamento deve receber ciclo civil (`cycleMonth` ou `cycleStart`) e não owner. O owner vem de `CurrentUser.id()`.

Respostas devem ser DTOs, contendo cartão, período, fechamento, vencimento, status, total, pago, devido e itens/parcela. IDs de outro owner retornam `404`.

Não expor endpoints de pagamento nesta task.

## Persistência e migrations

Próximas migrations disponíveis após V18 devem ser confirmadas no momento da implementação. Sugestão:

```text
V19__create_credit_card_statements.sql
V20__link_credit_card_installments_to_statements.sql
```

`credit_card_statements` deve conter FKs para owner e cartão, checks monetários/status, índice `(owner_id, credit_card_id, cycle_start)` e unicidade de ciclo:

```text
UNIQUE(owner_id, credit_card_id, cycle_start, cycle_end)
```

`credit_card_installments.statement_id` deve ter FK nullable para statement e índice `(owner_id, statement_id, sequence_number)`.

Não modificar V17/V18 já aplicadas. Todas as tabelas usam InnoDB, UUID consistente e `DECIMAL(19,2)`.

## Ownership, locking e idempotência

- owner sempre de `CurrentUser.id()`;
- cartão, fatura e parcelas devem pertencer ao mesmo owner;
- repositories devem oferecer queries owner-aware;
- ciclo único protegido por constraint;
- fechamento usa `@Transactional`;
- cartão/fatura usam lock apropriado durante associação;
- retry do fechamento retorna o recurso já fechado;
- concorrência não pode criar duas faturas nem duplicar parcelas;
- não aceitar `ownerId` no request;
- não revelar existência de recursos de outro usuário.

## Compatibilidade com compras parceladas

Compra de `R$ 1.200,00` em 3 parcelas deve resultar em três itens de `R$ 400,00`, cada um associado a um ciclo consecutivo. A compra total não deve ser lançada integralmente em todas as faturas.

Para `R$ 100,00 / 3`, os itens devem ser `R$ 33,34`, `R$ 33,33`, `R$ 33,33`, preservando a soma total. O fechamento soma somente as parcelas cujo `cycleMonth` pertence àquele ciclo.

Parcelas canceladas/revertidas não entram no total da fatura. Compra cancelada antes do fechamento não gera item devido. Fatura já fechada não deve mudar retroativamente por cancelamento sem política de ajuste; esse caso deve retornar conflito ou gerar fluxo futuro explícito.

## Testes previstos

### Calendário/domínio

- fechamento nos dias 1, 3, 28, 29, 30 e 31;
- fevereiro comum e bissexto;
- meses com 30 dias;
- vencimento dia 29/30/31 normalizado;
- compra antes do fechamento;
- compra exatamente no fechamento;
- compra depois do fechamento;
- timezone do cartão diferente do servidor.

### Fatura

- criação de fatura aberta;
- fechamento calcula total corretamente;
- fechamento repetido é idempotente;
- concorrência não duplica fatura/itens;
- parcelas associadas ao ciclo correto;
- compra parcelada não aparece integralmente em todos os ciclos;
- fatura de `R$ 600,00` não altera saldo;
- vencida não debita conta;
- status e valores retornam corretamente;
- User A não acessa fatura de B;
- IDs estrangeiros retornam 404;
- consulta/listagem paginada.

### Regressão

- compra bancária continua igual;
- compra cartão continua sem `FinancialTransaction`;
- limite não é liberado por fechamento;
- projeção/compromissos não recebem duplicatas;
- migrations Testcontainers/MySQL e backend build.

## Riscos

- **Alto:** associar parcela a ciclo errado por duplicação da lógica de datas. Mitigar com componente central e testes exaustivos.
- **Alto:** fechamento concorrente gerar duas faturas. Mitigar com unique constraint e lock.
- **Alto:** recalcular fatura fechada alterar histórico. Mitigar com transição terminal e conflito.
- **Médio:** dias inexistentes em vencimento. Mitigar com `YearMonth.lengthOfMonth()`.
- **Médio:** liberar limite no fechamento. Mitigar mantendo obrigação ativa até pagamento futuro.
- **Baixo:** custo de listar parcelas. Mitigar com índices e paginação.

## Fora do escopo

- pagamento total/parcial;
- juros, multas e encargos;
- frontend de faturas;
- projeção final de pagamentos;
- importação de faturas de emissor.

## Gate pré-implementação

A arquitetura necessária está definida e não há decisão essencial bloqueante para implementar faturas e fechamento. O pagamento permanece explicitamente fora da task.

**READY TO IMPLEMENT TASK-8.4.**
