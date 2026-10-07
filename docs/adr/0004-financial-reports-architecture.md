# ADR 0004 — Arquitetura dos Relatórios Financeiros

## Status

Aceita para orientar a Sprint 5. Este ADR não implementa endpoints, controllers, páginas, consultas finais, migrations ou exportação.

## Contexto

O Equilibra possui um ledger operacional unificado em `FinancialTransaction`, saldo inicial em `AssetAccount`, consultas de saldo/Dashboard e histórico paginado. A Sprint 5 precisa oferecer Relatório Financeiro, Relatório por Categoria e Relatório de Auditoria sem criar uma segunda definição de receita, despesa, transferência, saldo ou patrimônio.

## Decisão

Relatórios serão **read models derivados** da fonte de verdade existente:

```text
FinancialTransaction + initialBalance
```

A implementação futura terá serviços de aplicação/query dedicados e projeções/agregações específicas. Não serão persistidos totais de relatório, snapshots prematuros ou movements duplicados. Um `AccountMovement` conceitual pode ser usado internamente em uma projeção de consulta, mas não será uma nova fonte de verdade.

## Escopo comum

### Período

- `from` e `to` são obrigatórios no Relatório Financeiro e no Relatório por Categoria.
- O intervalo é `[from, to)`: `occurredAt >= from` e `occurredAt < to`.
- `from >= to` retorna `400 Bad Request`.
- Competência financeira usa `occurredAt`, nunca `createdAt`.
- O backend trabalha com `Instant`/UTC; o frontend envia instantes com offset explícito e exibe no timezone da pessoa usuária.
- A UI pode iniciar no mês atual, mas isso é default de apresentação, não uma regra financeira da API.
- Não há limite arbitrário de período nesta decisão; queries devem ser agregadas e paginadas.

### Contas

- O cliente pode enviar zero, um ou vários `accountIds`.
- Lista ausente ou vazia significa todas as contas elegíveis do owner.
- IDs duplicados são deduplicados deterministically antes da consulta.
- Contas ativas e inativas próprias podem ser selecionadas, pois desativação não apaga histórico.
- Cada ID é validado por `id + CurrentUser.id()` antes da consulta; ID inexistente ou de outro owner retorna `404` conforme política privada atual.
- Nunca aceitar `ownerId` ou `userId` no contrato.
- Para uma transferência, o filtro de conta corresponde quando source **ou** destination está selecionada.

### Status e cancelamento

- Consultas financeiras usam ACTIVE por padrão.
- `CANCELLED` tem efeito financeiro zero.
- O Relatório de Auditoria inclui ACTIVE e CANCELLED por padrão para rastreabilidade, com filtro explícito `ALL | ACTIVE | CANCELLED`.
- History pode manter seu default ACTIVE; a diferença é intencional e deve ser visível no contrato.

### Tags, categorias e attachments

- Filtros de tags, quando suportados, aceitam `0..N tagIds`, validam ownership e reutilizam a semântica ANY do History.
- Filtros de categoria validam ownership e seguem o History.
- Categorias inativas e tags inativas associadas historicamente permanecem reportáveis.
- `CategoryApplicability.BOTH` produz totais de Income e Expense separados; nunca colapsar valores opostos em zero sem composição.
- Anexos são representados somente por `attachmentCount` ou metadata segura. Nunca bytes, storage key, path físico, checksum desnecessário ou URL pública.

## Relatório Financeiro

### Objetivo

Analisar um período e um conjunto de contas com saldo de abertura, fluxos, transferências, resultado financeiro, variação de saldo, saldo de fechamento e detalhes paginados.

### Transferências e fronteira da seleção

Para uma transferência:

- source selecionada e destination selecionada: `internalTransfer`; efeito agregado zero;
- source não selecionada e destination selecionada: `incomingBoundaryTransfer`;
- source selecionada e destination não selecionada: `outgoingBoundaryTransfer`;
- nenhuma das contas selecionada: não entra no escopo.

Transferência nunca é classificada como Income ou Expense.

### Métricas

```text
financialResult = income - expense

netBalanceChange = income
                  - expense
                  + incomingBoundaryTransfers
                  - outgoingBoundaryTransfers

closingBalance = openingBalance + netBalanceChange
```

Transferências internas têm contribuição zero. O saldo de abertura de cada conta selecionada é sua posição imediatamente antes de `from`:

```text
openingBalance(account, from) = initialBalance
  + ACTIVE income recebida antes de from
  - ACTIVE expense da conta antes de from
  + ACTIVE transfer recebida antes de from
  - ACTIVE transfer enviada antes de from
```

O saldo de fechamento aplica os efeitos ACTIVE dentro do período. A equação deve ser verificada nos testes e o summary deve considerar todas as linhas filtradas, não somente a página de detalhes.

### Estrutura conceitual

```text
period
selectedAccounts
openingBalance
income
expense
financialResult
incomingBoundaryTransfers
outgoingBoundaryTransfers
internalTransfers
netBalanceChange
closingBalance
transactions: paged logical FinancialTransaction details
```

Detalhes usam uma transação lógica por linha; uma transferência não aparece duas vezes por causa de seus efeitos.

## Relatório por Categoria

Agrupa ACTIVE Income e Expense do período por categoria, com:

```text
categoryId
categoryName
incomeTotal
expenseTotal
netResult = incomeTotal - expenseTotal
```

Categoria `BOTH` aparece em ambos os lados quando houver os dois tipos. Transferências não entram nos totais de Income/Expense; sua categoria não deve ser forçada em um KPI incompatível. Categoria inativa e renomeada continua histórica; usa-se o nome atual porque o modelo não possui snapshot de nome.

O filtro pode aceitar uma ou várias categorias owner-scoped e os mesmos accountIds, período e tagIds definidos pelo contrato comum.

## Relatório de Auditoria

É um read model paginado para rastreabilidade das transações do owner. Inclui por padrão ACTIVE e CANCELLED e permite filtrar status, tipo, período, uma/várias contas, categoria e tags seguindo History.

Campos seguros conceituais:

- transaction ID;
- type/status;
- description;
- occurredAt;
- amount;
- source/destination ou conta aplicável;
- category e tags;
- attachmentCount;
- notes;
- createdAt/updatedAt.

A auditoria não é debug do ledger: não expõe SQL, stack trace, JWT, storage key, filesystem, bytes ou IDs técnicos sem utilidade. A ordenação é `occurredAt DESC, id DESC`; detalhes são server-side paginated e transferências permanecem uma linha.

## Summary e detalhes

A API futura deve separar claramente aggregate summary e paged details. A decisão preferida é um endpoint composto por relatório, contendo `summary` e `transactions`, desde que uma consulta/serviço não limite o summary à página. Endpoints separados são aceitáveis se reduzirem complexidade e forem documentados; não criar requests redundantes sem necessidade.

Nenhum total pode ser calculado no Angular ou sobre somente os itens carregados.

## API, segurança e erros

- APIs de relatórios são privadas por padrão e exigem JWT.
- Ausência/JWT inválido: `401`.
- Filtro inválido, período vazio/invertido ou enum inválido: `400` com Problem Details.
- ID privado de conta/categoria/tag: `404`, sem revelar existência.
- Owner sempre vem de `CurrentUser.id()`.
- DTOs de filtro não aceitam campos de propriedade/autorização.
- Valores usam `BigDecimal` no backend e `DECIMAL(19,2)` na persistência existente; não arredondar intermediariamente nem usar `double` no cálculo.

## Read model e performance

- Criar query/application services no módulo `report`; não concentrar relatórios em `FinancialTransactionRepository` sem necessidade.
- Usar agregação no banco para abertura, summary e `GROUP BY` por categoria.
- Usar paginação no banco para auditoria/detalhes.
- Usar projections/DTOs tipados; SQL nativo MySQL é permitido para agregações complexas quando owner-scoped, documentado e coberto por Testcontainers.
- Carregar tags e attachment counts por batch/projection; evitar N+1.
- Reutilizar filtros/semântica do History, mas não duplicar seu cálculo em Java.
- Avaliar índices adicionais somente após medir queries reais; TASK-5.0 não cria migration.

Índices atuais de `financial_transactions` por owner/occurredAt, status, type, source, destination e category, além dos índices de tags/anexos, são o ponto de partida.

## Consistência obrigatória

Para owner, período e todas as contas equivalentes:

```text
Financial Report income = Dashboard Entradas
Financial Report expense = Dashboard Saídas
Financial Report financialResult = Dashboard Saldo
```

A comparação com patrimônio requer distinguir:

- Dashboard `netWorth`: posição atual, atualmente sobre contas ativas;
- report `closingBalance`: posição no final de `to` para as contas selecionadas, incluindo contas inativas quando selecionadas.

Audit deve concordar com History para o mesmo filtro, exceto pelo default explícito de status.

## Frontend futuro

Rotas conceituais, adaptadas às convenções existentes:

```text
/reports/financial
/reports/categories
/reports/audit
```

A navegação privada terá o grupo Relatórios. Um componente compartilhado de filtros pode conter período e multiselect de contas ativas/inativas, com botão **Aplicar filtros** e **Limpar filtros**. Query params podem preservar filtros em refresh/back-forward, sem incluir ownerId. Loading, erro e vazio são estados distintos. Mobile usa cards/listas; tabelas não devem ser comprimidas até ficarem ilegíveis. Gráficos são opcionais e nunca a única representação dos dados; tabelas/texto acessíveis são obrigatórios.

## Estratégia de testes

Cada task de implementação deve testar:

1. período `[from,to)` e `occurredAt`;
2. from/to inválidos;
3. todas, uma e várias contas;
4. IDs duplicados, conta inativa e cross-owner;
5. ACTIVE/CANCELLED;
6. Income, Expense, transferências internas e de fronteira;
7. equação opening + change = closing;
8. categoria BOTH, categoria inativa e renomeada;
9. tags ANY e ownership;
10. attachmentCount sem vazamento;
11. paginação sem alterar totals;
12. Transfer uma única vez;
13. dois owners isolados;
14. precisão `0.10 + 0.20 = 0.30`;
15. consistência com Dashboard e History.

### Golden Scenario obrigatório

Contas A=1000, B=500, C=200. Antes do período, Income A=100, portanto abertura A=1100, B=500, C=200. No período:

- Income A +700;
- Expense A -200;
- Transfer A→B 300;
- Transfer C→A 50.

Todas as contas: opening 1800, income 700, expense 200, internal transfers 350, boundary transfers 0, financial result 500, balance change 500, closing 2300.

A isolada: opening 1100, income 700, expense 200, incoming 50, outgoing 300, closing 1350. B fecha 800; C fecha 150; soma 2300.

## Fora de escopo e decisões adiadas

Não implementar nesta ADR: controllers, queries finais, páginas, gráficos, exportação, PDF/CSV/XLSX, snapshots, multi-moeda, budgets, recorrência, novo ledger, data warehouse, Elasticsearch, auditoria de eventos técnicos ou migrations. Idempotency-Key e limite máximo de período ficam para avaliação quando os endpoints forem implementados.

## Consequências

A arquitetura mantém uma única fonte de verdade e permite que Dashboard, History e Reports sejam comparados. Em troca, relatórios exigirão queries agregadas cuidadosas, read models tipados e testes MySQL/Testcontainers. Otimizações persistentes só poderão ser introduzidas após medição, sem substituir o ledger.
