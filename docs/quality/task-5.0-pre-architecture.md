# TASK-5.0 — PRE-ARCHITECTURE REPORT

## Status

**CONCLUÍDA — somente arquitetura/documentação**

Esta task não implementa controllers, endpoints, páginas, queries finais, gráficos, exportação ou migrations. O objetivo é registrar o modelo real e as decisões executáveis para TASK-5.1 a TASK-5.8.

## Ledger

A fonte de verdade financeira é:

```text
initialBalance + efeitos das FinancialTransaction ACTIVE
```

Não existe coluna mutável `currentBalance` nem uma tabela de totais de relatório. `FinancialTransaction` é o registro lógico unificado; `AccountMovement` não é um agregado independente no código atual. Os efeitos são derivados dos campos source/destination:

- source: saída (`-amount`);
- destination: entrada (`+amount`);
- uma transferência pode produzir os dois efeitos, mas continua uma transação lógica.

`CANCELLED` permanece no histórico, mas tem efeito financeiro zero.

## FinancialTransaction

A entidade possui UUID string, `ownerId`, `type` (`EXPENSE`, `INCOME`, `TRANSFER`), `status` (`ACTIVE`, `CANCELLED`), descrição, `occurredAt: Instant`, `amount: BigDecimal` positivo com escala máxima 2, categoria, contas source/destination, notas, timestamps, `cancelledAt`, `@Version` e `tagIds` como `@ElementCollection`.

Invariantes persistidas e de domínio exigem:

- EXPENSE: source obrigatório, destination nulo;
- INCOME: source nulo, destination obrigatório;
- TRANSFER: source e destination obrigatórios e diferentes;
- amount positivo e não zero;
- referências validadas pelo owner na aplicação;
- cancelamento lógico e terminal.

## AccountMovement / sinais

A implementação atual não possui uma entidade `AccountMovement` separada; o comportamento equivalente é derivado diretamente da transação central. Relatórios futuros não devem fabricar uma segunda coleção de movements. Quando uma abstração de movement for necessária para consulta, ela deve ser uma projeção/read model derivada, não uma nova fonte de verdade.

## Current Balance

`AccountBalanceQueryService` carrega contas do owner e transações ACTIVE, acumulando separadamente `-amount` para source e `+amount` para destination, e soma o resultado ao `initialBalance`. O serviço pode incluir ou excluir contas inativas para a lista conforme o parâmetro, mas o Dashboard atual calcula patrimônio sobre contas ativas.

Conceito para relatórios:

```text
balanceAt(account, instant) = initialBalance
  + incomes recebidas até instant
  - expenses da conta até instant
  + transfers recebidas até instant
  - transfers enviadas até instant
```

## Dashboard

`DashboardQueryService` exige `from < to` e usa `occurredAt` para o período. Entradas são a soma de INCOME ACTIVE no intervalo; Saídas são a soma de EXPENSE ACTIVE; Saldo é `Entradas - Saídas`. Transferências não entram nesses dois KPIs. O patrimônio atual é calculado separadamente a partir dos saldos derivados das contas ativas, usando todos os movimentos ACTIVE; a lista de transações recentes é limitada apenas para apresentação.

Consequentemente, o patrimônio atual do Dashboard não é automaticamente o saldo de fechamento de um período histórico.

## History

`TransactionHistoryService` usa paginação server-side, ordenação estável por `occurredAt DESC, id DESC`, filtros de tipo, status, período, conta, categoria e tags. O filtro de conta usa source OR destination, preservando uma transferência como uma linha. O intervalo temporal é `occurredAt >= from` e `< to`.

A resposta contém transação lógica, summaries de tags owner-scoped e `attachmentCount` agregado em batch. O histórico não abre bytes nem expõe storage metadata. Tags múltiplas reutilizam a semântica ANY já implementada (`tagIds` repetidos na query).

## Category

`Category` é owner-scoped, possui `CategoryApplicability` (`EXPENSE`, `INCOME`, `BOTH`), lifecycle ativo/inativo e nome normalizado. Novos lançamentos validam applicability e categoria ativa. Categoria inativa não pode ser escolhida em novo lançamento, mas permanece referenciável no histórico. Categorias `BOTH` podem aparecer em agrupamentos de receitas e despesas separadamente; não se deve colapsar os dois sentidos sem composição explícita.

## Tags

Tags são metadata owner-scoped com lifecycle active/inactive e associação N:N lógica em `financial_transaction_tags`. A desativação preserva associações históricas; nova associação exige tag ativa. O filtro do History valida todos os IDs no owner autenticado e usa semântica ANY. Relatórios podem preparar `tagIds`, mas devem reutilizar essa semântica, sem inventar ALL.

## Attachments

`TransactionAttachment` relaciona owner, transaction, nome seguro, storage key privada, MIME, tamanho, checksum e timestamp. O conteúdo fica no storage local configurável; o banco guarda metadata. Relatórios só podem mostrar `attachmentCount` ou metadata segura. Nunca devem retornar bytes, storage key, path físico, checksum desnecessário ou links públicos.

## Period semantics e timezone

`occurredAt` é `Instant`, recebido com offset explícito e normalizado pelo backend para UTC. `createdAt` é timestamp técnico e não define competência financeira. Relatórios financeiros devem usar intervalo `[from, to)`, com `from < to`; igualdade ou inversão são erro 400. A UI pode exibir no timezone local, mas a fronteira enviada deve representar instantes explícitos. Não introduzir novo timezone ou nova semântica de dia.

## Ownership

O owner vem exclusivamente de JWT → SecurityContext → `CurrentUser.id()`. IDs de conta, categoria e tag enviados pelo cliente são apenas filtros e devem ser validados com queries owner-scoped antes da consulta. ID privado de outro owner/inexistente segue a política existente de `404`, sem revelar existência. Nenhum endpoint aceita `ownerId` ou `userId` para autorização.

## Performance e queries reutilizáveis

- `FinancialTransactionRepository` já possui índices e consulta paginada para History.
- `AccountBalanceQueryService` e `DashboardQueryService` expressam os efeitos financeiros atuais, mas não devem ser chamados em loop por conta.
- Resumos financeiros e abertura/fechamento devem ser calculados por agregação no banco, não carregando todas as transações anteriores em Java.
- Relatórios por categoria devem usar `GROUP BY`/projeções tipadas.
- Auditoria deve ser paginada no banco.
- Detalhes devem carregar tags e attachment counts em batch/projeção, evitando N+1.
- Summary deve considerar todo o conjunto filtrado, independentemente da página de detalhes.
- SQL nativo MySQL é aceitável para agregações complexas se owner-scoped, tipado, documentado e coberto por Testcontainers.

## Índices existentes relevantes

`V5__create_financial_transactions.sql` possui:

- `(owner_id, occurred_at)`;
- `(owner_id, status, occurred_at)`;
- `(owner_id, type, occurred_at)`;
- `(owner_id, source_account_id, occurred_at)`;
- `(owner_id, destination_account_id, occurred_at)`;
- `(owner_id, category_id, occurred_at)`.

`V7` possui índices para `tag_id` e `transaction_id` na tabela de associação. `V8` possui índices de owner/transaction e unique storage key para attachments. Não há justificativa nesta task para criar migration; índices compostos adicionais devem ser medidos contra queries reais nas tasks de implementação.

## Riscos

1. Duplicar a fórmula de saldo em cada relatório e divergir do Dashboard.
2. Classificar transferências como receita/despesa.
3. Contar transferências internas duas vezes ou duplicar linhas por movement.
4. Calcular summary apenas sobre a página atual.
5. Excluir contas/categorias inativas e perder histórico.
6. Filtrar owner em Java após buscar dados de outros usuários.
7. Criar N+1 para tags, categorias ou anexos.
8. Confundir posição atual com fechamento histórico.
9. Misturar `createdAt` com competência financeira.
10. Expor storage key, path ou bytes na auditoria.
11. Criar semântica diferente de filtros de History.
12. Alterar o ledger ou persistir totais derivados.

## Fora de escopo

Controllers, DTOs finais, endpoints, serviços de relatório, tabelas/persistência de totais, novas migrations, páginas, exportação, PDF/CSV/XLSX, gráficos, snapshots, multi-moeda e novo mecanismo de auditoria.
