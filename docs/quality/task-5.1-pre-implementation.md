# TASK-5.1 — PRE-IMPLEMENTATION REPORT

## 1. ADR 5.0

O ADR vigente é `docs/adr/0004-financial-reports-architecture.md`. Para esta task, o Financial Report será um read model privado, sem persistência de totais ou segundo ledger. O período usa `occurredAt` e `[from,to)`, com `from`/`to` obrigatórios e `from < to`. A seleção de contas aceita uma, várias ou todas; contas inativas próprias permanecem selecionáveis; IDs externos/inexistentes retornam 404; duplicatas são deduplicadas; ausência/vazio significa todas.

Transferências serão classificadas de acordo com o conjunto selecionado: internas (source e destination selecionadas, efeito agregado zero), incoming boundary e outgoing boundary. O relatório separa financial result (`income - expense`) de balance change e valida `closing = opening + balanceChange`.

## 2. Ledger

`FinancialTransaction` é a fonte lógica unificada. `amount` é magnitude positiva; source aplica saída e destination entrada. Apenas ACTIVE participa. initialBalance pertence à posição da conta e nunca a income. Não será criada tabela de report movements.

## 3. Endpoint

```text
GET /api/reports/financial
```

Parâmetros obrigatórios: `from`, `to` em ISO date-time. Parâmetros opcionais: `accountIds` repetido, `page` (default 0) e `size` (default 20, máximo 100). O contrato não aceita ownerId/userId/email. A resposta é composta por summary global e detalhes paginados.

## 4. Filters

- `from`, `to`: obrigatórios, `[from,to)` sobre occurredAt;
- `accountIds`: ausente/vazio = todas as contas do owner; repetidos deduplicados; ativas e inativas próprias;
- filtros de tags/categorias: não adicionados ao contrato mínimo desta task, pois o ADR os deixa preparados para tasks específicas; não inventar query adicional;
- `page`/`size`: somente detalhes, nunca afetam summary.

## 5. Opening Balance

Para cada conta selecionada, o serviço soma initialBalance aos efeitos ACTIVE anteriores a `from`: income recebido, expense financiada, transfer recebida e transfer enviada. A agregação será realizada em query dedicada/projection no banco, não carregando todo o histórico em Java.

## 6. Income

Income total é a soma de INCOME ACTIVE dentro do período e do conjunto selecionado. Para o conjunto de contas, um income só entra quando sua destination pertence à seleção.

## 7. Expense

Expense total é a soma de EXPENSE ACTIVE dentro do período e do conjunto selecionado. Só entra quando sua source pertence à seleção. Magnitudes permanecem positivas no summary.

## 8. Transfers

- internal: source e destination selecionadas; magnitude contabilizada em internalTransferTotal, mas efeito líquido zero;
- incoming boundary: destination selecionada e source fora; soma positiva para balance change;
- outgoing boundary: source selecionada e destination fora; soma negativa para balance change;
- nenhuma transferência é classificada como income/expense;
- details retornam uma única FinancialTransaction lógica.

## 9. Financial Result

```text
financialResult = incomeTotal - expenseTotal
```

Não inclui transferências.

## 10. Balance Change

```text
balanceChange = incomeTotal
              - expenseTotal
              + incomingBoundaryTransferTotal
              - outgoingBoundaryTransferTotal
```

Internal transfers contribuem zero.

## 11. Closing Balance

```text
closingBalance = openingBalance + balanceChange
```

O serviço e os testes verificarão essa invariável explicitamente.

## 12. Transaction Details

Detalhes serão paginados no banco, ordenados por `occurredAt DESC, id DESC`, uma linha por FinancialTransaction. Transfer aparece uma vez. A resposta pode reutilizar os campos seguros do History, sem expor entidades, ownerId, storage key, path ou bytes.

## 13. Ownership

O owner será obtido exclusivamente de `CurrentUser.id()`. Antes das queries, todos os accountIds explícitos serão carregados por `findByIdAndOwnerId`; qualquer ausência retorna `ResourceNotFoundException`/404. Nenhum filtro aceita ownerId.

## 14. Query Strategy

Serão criados repository/query objects dedicados ao módulo report, com projections/agregações MySQL para:

1. contas selecionadas e initial balances;
2. opening effects anteriores a from;
3. flow classification dentro do intervalo;
4. details paginados;
5. counts/metadata segura em batch, se details exibirem anexos/tags.

Não ampliar `FinancialTransactionRepository` com um god repository.

## 15. Performance

Summary não depende de details page. Não haverá paginação em memória, load-all anterior ao período ou N+1. Agregações serão executadas no banco; detalhes usarão `Pageable`. Tags/anexos, se incluídos, serão projeção/batch owner-scoped.

## 16. Index Review

Serão reutilizados índices atuais V5: owner/occurredAt, owner/status/occurredAt, owner/type/occurredAt, owner/source/occurredAt, owner/destination/occurredAt e owner/category/occurredAt. V7/V8 já cobrem associações e anexos. Nenhuma migration é planejada antes de medir as queries reais.

## 17. Tests

Cobertura planejada com MySQL/Testcontainers:

- 400 para período ausente/inválido/revertido;
- 401 sem autenticação;
- uma, várias, todas e contas inativas;
- duplicidade deduplicada;
- cross-owner/nonexistent account 404;
- boundaries from/to;
- opening/closing e initialBalance fora de income;
- income/expense ACTIVE e CANCELLED;
- transfer interna, incoming e outgoing sem dupla contagem;
- precisão BigDecimal;
- página não altera summary e Transfer aparece uma vez;
- isolamento entre usuários;
- reconciliação com Dashboard para mesmo owner/período/todas as contas;
- tags/attachments sem efeito financeiro.

## 18. Files

### Criar

- `backend/src/main/java/br/com/equilibra/report/api/*`;
- `backend/src/main/java/br/com/equilibra/report/application/*`;
- `backend/src/main/java/br/com/equilibra/report/infrastructure/*`;
- testes do módulo report;
- relatório final `docs/quality/task-5.1-financial-report.md`.

### Modificar

Somente arquivos necessários para registrar o endpoint e o módulo. Nenhum frontend, migration, Dashboard ou History será alterado sem bug comprovado.

### Reutilizar

`CurrentUser`, Problem Details, Request ID, entidades/repositories financeiros existentes, sem expor entidades diretamente.
