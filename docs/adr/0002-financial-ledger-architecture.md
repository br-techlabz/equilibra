# ADR 0002 — Ledger financeiro derivado para transações

## Status

Aceita para orientar a Sprint 3. Esta decisão não implementa entidades, migrations, endpoints ou telas financeiras.

## Contexto

O Equilibra concluiu a Sprint 2 com usuários autenticados, isolamento por `CurrentUser`, `AssetAccount`, `Category` e um Dashboard inicial. `AssetAccount` possui `initialBalance` em `BigDecimal`/`DECIMAL(19,2)`, mas ainda não há transações financeiras, ledger ou `currentBalance` derivado de movimentações.

O sistema precisa suportar despesas, receitas, transferências, histórico, edição, cancelamento, relatórios e auditoria sem transformar um saldo mutável em uma segunda fonte de verdade.

## Diagnóstico do estado atual

- `AssetAccount`: agregado privado com UUID string, `ownerId`, nome normalizado, tipo, `initialBalance`, `active`, timestamps e versão otimista. A API e os repositories usam ownership-aware queries.
- `Category`: agregado privado com UUID string, `ownerId`, nome normalizado, `CategoryApplicability` (`EXPENSE`, `INCOME`, `BOTH`), `active`, timestamps e versão. A API valida ownership e filtros de applicability.
- Ownership: a identidade confiável vem de JWT → SecurityContext → `CurrentUser.id()`. O cliente não envia proprietário.
- Dinheiro: backend usa `BigDecimal`; contas usam `DECIMAL(19,2)` e rejeitam escala superior a duas casas. O frontend calcula o patrimônio temporário em centavos inteiros apenas para apresentação.
- Dashboard: soma `initialBalance` das contas ativas. Entradas, saídas, saldo, gráfico e transações permanecem neutros por não existir ledger.
- Migrations: Flyway incremental, MySQL/InnoDB, Hibernate em validação. Não há migration financeira.
- Auditabilidade: entidades atuais preservam `createdAt`, `updatedAt` e `@Version`; não há framework de auditoria nem histórico de transações.

## Decisão

Adotar uma entidade conceitual central `FinancialTransaction`, persistida futuramente como um ledger simples unificado, com `TransactionType` e `TransactionStatus`. Despesas, receitas e transferências serão casos de uso explícitos que validam invariantes próprias, mas compartilharão histórico, consulta temporal, ownership e ciclo de vida.

A fonte de verdade será:

```text
initialBalance + efeitos das transações ACTIVE do ledger
```

Não persistir `currentBalance` mutável em `AssetAccount` nesta fase. O saldo será derivado por consulta/serviço. Assim, uma transação criada, editada ou cancelada não exige uma atualização manual concorrente do saldo.

Não adotar double-entry bookkeeping completo no MVP: o domínio doméstico precisa de um ledger operacional simples, não de um plano contábil geral. Transferências ainda serão atômicas e terão origem/destino na mesma operação de banco.

## Modelo conceitual

`FinancialTransaction` deverá conter, no mínimo:

- `id`: UUID string;
- `ownerId`: UUID string, obrigatório e imutável;
- `type`: `EXPENSE`, `INCOME` ou `TRANSFER`;
- `status`: `ACTIVE` ou `CANCELLED`;
- `description`: texto obrigatório, trim, máximo 255 caracteres;
- `occurredAt`: instante informado do evento;
- `amount`: `BigDecimal` positivo, máximo duas casas;
- `categoryId`: referência privada à Category, com política de obrigatoriedade definida abaixo;
- `sourceAccountId`: referência privada à AssetAccount quando houver saída;
- `destinationAccountId`: referência privada à AssetAccount quando houver entrada;
- `notes`: texto opcional, multiline, limite definido na task de implementação;
- `createdAt`, `updatedAt`: timestamps técnicos UTC;
- `cancelledAt`: recomendado quando status muda para `CANCELLED`;
- `version`: `@Version` para detectar edição concorrente.

Referências serão UUIDs e serão validadas ownership-aware na camada de aplicação. A entidade não deve aceitar IDs de outro owner nem depender de HTTP.

## Tipos e invariantes

| Tipo | Origem | Destino | Categoria | Efeito no saldo |
| --- | --- | --- | --- | --- |
| `EXPENSE` | obrigatória, ativa no momento da criação | proibido | `EXPENSE` ou `BOTH`, ativa na criação | origem − amount |
| `INCOME` | proibida | obrigatória, ativa no momento da criação | `INCOME` ou `BOTH`, ativa na criação | destino + amount |
| `TRANSFER` | obrigatória | obrigatória e diferente da origem | `BOTH` recomendado para representar contexto neutro; política final deve ser validada antes da implementação | origem − amount; destino + amount |

Todas as referências devem pertencer ao mesmo `ownerId` da transação. Conta ou categoria inativa não pode ser selecionada para novo lançamento, mas a desativação posterior não invalida o histórico.

`amount` é sempre positivo e o tipo determina o efeito. Zero não é permitido. Sinal negativo não representa despesa. Moeda é implicitamente BRL nesta versão e não será persistida enquanto o produto for explicitamente BRL.

A obrigatoriedade de categoria para `TRANSFER` permanece uma decisão de implementação da próxima task: o requisito menciona categoria, mas transferência não é receita nem despesa. A recomendação é exigir uma categoria `BOTH` para manter classificação explícita sem contá-la nos KPIs de entrada/saída.

## Saldo, patrimônio e KPIs

Para uma conta `A` em um instante `T`:

```text
balanceAt(A, T) = initialBalance(A)
  + soma(INCOME ACTIVE recebidas por A até T)
  - soma(EXPENSE ACTIVE financiadas por A até T)
  + soma(TRANSFER ACTIVE recebidas por A até T)
  - soma(TRANSFER ACTIVE enviadas por A até T)
```

Transações `CANCELLED` não participam. `occurredAt` define a posição temporal; `createdAt` não fabrica histórico.

```text
netWorth(T) = soma dos balanceAt(A, T) das contas que participam do patrimônio
```

No curto prazo, `active=false` significa indisponível para novos lançamentos. A recomendação para o ledger é não apagar histórico nem excluir automaticamente o saldo histórico de uma conta apenas por desativá-la; a política de participação no patrimônio deve ser definida explicitamente na task de saldo/Dashboard. Até essa decisão, o Dashboard atual continua usando apenas contas ativas para o patrimônio temporário baseado em `initialBalance`.

```text
Entradas = soma de INCOME ACTIVE no período
Saídas   = soma de EXPENSE ACTIVE no período
Saldo    = Entradas - Saídas
```

Transferências não entram em Entradas ou Saídas e não alteram o patrimônio total quando origem e destino pertencem ao mesmo owner.

## Ciclo de vida e edição

- Não haverá `DELETE` físico como operação principal.
- `CANCELLED` preserva o registro, não afeta saldo e mantém histórico/auditoria.
- Reativação de cancelada não deve ser adicionada por simetria sem requisito; a recomendação é cancelamento terminal e, se necessário, criação de transação corretiva.
- Edição de uma transação `ACTIVE` altera seus campos dentro de uma transação de banco e o saldo derivado muda naturalmente.
- `cancelledAt` deve ser preenchido quando cancelada; quem alterou pode ser adicionado em auditoria futura, mas não é obrigatório no MVP multiusuário porque o owner autenticado executa a operação.
- Transferência deve ser criada/editada/cancelada atomicamente; nunca persistir débito sem crédito lógico correspondente.

## Ownership e consistência

A aplicação deverá obter o owner exclusivamente de `CurrentUser.id()`. Para cada operação:

1. carregar a transação por `id + ownerId`;
2. carregar cada conta e categoria por `id + ownerId`;
3. validar active/status e applicability;
4. persistir a operação dentro de `@Transactional`.

Nunca aceitar `ownerId`, `userId` ou e-mail do proprietário no request. Usuário A não poderá referenciar conta/categoria de B. Recursos de outro owner devem preferencialmente resultar em `404`.

## Initial balance após o ledger

Recomendação para o MVP: depois que existir a primeira transação de uma conta, não permitir editar `AssetAccount.initialBalance` diretamente. A alternativa mais auditável no futuro é representar o saldo inicial como evento/ajuste do ledger, ou oferecer um ajuste explícito que preserve motivo e data. Manter a edição atual enquanto não houver transações; a próxima task deve introduzir a regra sem reescrever histórico silenciosamente.

## Category applicability após histórico

Uma mudança de applicability que tornaria uma categoria incompatível com transações existentes não deve corromper o histórico. Recomendação simples: permitir mudanças enquanto não houver uso incompatível; depois bloquear a alteração ou criar uma nova categoria. Não migrar transações automaticamente nesta fase.

## Timezone e data

`occurredAt` representa o instante real do evento e será recebido com offset/instante explícito, normalizado para UTC (`Instant`) no backend. A UI exibirá no timezone do usuário/sistema. `createdAt`, `updatedAt` e `cancelledAt` também permanecem em UTC.

Não permitir transações futuras por padrão; a próxima task deve confirmar se existe necessidade de agendamento antes de aceitar `occurredAt` futuro.

## API futura

Mesmo com persistência unificada, recomenda-se expor casos de uso claros:

```text
POST /api/expenses
POST /api/incomes
POST /api/transfers
GET  /api/transactions
GET  /api/transactions/{id}
PUT  /api/expenses/{id}       (ou endpoint unificado documentado)
PUT  /api/incomes/{id}
PUT  /api/transfers/{id}
PATCH /api/transactions/{id}/cancel
```

A separação dos POSTs torna os invariantes de cada tipo explícitos; `GET /transactions` fornece histórico unificado. A decisão final de contratos e DTOs pertence à task de domínio/API, não a esta ADR.

## Índices e consultas futuras

Planejar índices compostos ownership-aware para:

- `(owner_id, occurred_at)`;
- `(owner_id, status, occurred_at)`;
- `(owner_id, type, occurred_at)`;
- `(owner_id, category_id, occurred_at)`;
- `(owner_id, source_account_id, occurred_at)`;
- `(owner_id, destination_account_id, occurred_at)`.

A consulta de saldo deve filtrar `ownerId`, `status=ACTIVE`, intervalo temporal e conta. Não persistir pontos de gráfico como fonte de verdade; gráficos derivam do ledger.

## Frontend futuro

Ter três experiências de formulário (Despesa, Receita, Transferência) com campos visuais compartilhados, sem criar DynamicForm prematuramente. Reutilizar componentes pequenos demonstradamente comuns; manter regras de contas separadas. Selects devem carregar somente contas ativas e categorias ativas aplicáveis. Em Transferência, o destino deve excluir a conta de origem.

Tags serão relação futura, provavelmente N:N após requisito próprio. Anexos devem persistir metadata no banco e conteúdo em storage apropriado, não BLOB automaticamente.

## Alternativas rejeitadas

### Agregados/tabelas Expense, Income e Transfer separados

Rejeitados como arquitetura principal porque duplicam histórico, filtros, ownership, timestamps e auditoria; tornam consultas cronológicas unificadas e relatórios mais complexos. Podem continuar existindo como casos de uso/API sobre a entidade central.

### `AssetAccount.currentBalance` mutável

Rejeitado porque cria segunda fonte de verdade e permite divergência em falhas, concorrência, edição e cancelamento.

### Double-entry bookkeeping completo

Adiado: adicionaria contas contábeis, lançamentos balanceados e complexidade que o MVP doméstico ainda não exige.

### Sinal do amount para representar tipo

Rejeitado porque mistura magnitude e semântica; `amount` positivo + `type` explícito mantém invariantes claras.

## Concorrência e idempotência

Como saldo é derivado, duas despesas concorrentes não produzem lost update em `currentBalance`. A unicidade/ownership continuam protegidos pelo banco e transações. `@Version` é recomendado em `FinancialTransaction` para edição concorrente. `Idempotency-Key` fica adiado até haver integração/retries que justifiquem a proteção; a próxima API deve reavaliar duplicação de POST.

## Riscos

- **HIGH:** referências cross-owner ou validação fora de transação podem vazar dados ou criar transações inválidas; mitigar com CurrentUser e queries ownership-aware.
- **HIGH:** transferência parcialmente persistida; mitigar com agregado/operação única e `@Transactional`.
- **MEDIUM:** mudança de initialBalance/applicability após histórico; mitigar com bloqueio ou ajuste explícito.
- **MEDIUM:** timezone incorreto em `occurredAt`; mitigar com Instant/UTC e contrato explícito.
- **MEDIUM:** duplicação de POST em retry; reavaliar Idempotency-Key.
- **LOW:** custo de recalcular saldo em volume alto; snapshots/agregações somente após medição.

## Decisões adiadas

Tags, attachments/storage, recorrência, multi-moeda, budgets, double-entry, snapshots, reconciliação, importação bancária, parcelamento, cartão/fatura, limite de cheque especial, agendamento futuro, `Idempotency-Key`, política definitiva de conta inativa no patrimônio e interpretação de “Contas de Despesas”. Esta última pode significar favorecido, fornecedor, obrigação ou classificação e não será modelada como AssetAccount sem requisito específico.

## Catálogo de testes para próximas tasks

- INCOME aumenta saldo da conta destino.
- EXPENSE reduz saldo da conta origem.
- TRANSFER reduz origem e aumenta destino.
- TRANSFER não altera patrimônio total.
- TRANSFER não entra em Entradas/Saídas.
- CANCELLED não afeta saldo.
- Cross-user em transação, contas e categorias retorna 404.
- Conta/categoria inativa bloqueia novo lançamento.
- Histórico permanece válido após desativação.
- Amount positivo, não zero, escala máxima e precisão decimal.
- Edição altera saldo derivado.
- `balanceAt(T)` respeita occurredAt e timezone.
- Transferência rejeita mesma origem/destino.
- Transferência atômica em falha.
- Applicability incompatível bloqueada após uso histórico.
- InitialBalance protegido após primeiro lançamento.
- Filtros por período, conta, categoria e tipo.

## Consequências

A arquitetura mantém histórico financeiro unificado, regras de tipo explícitas e saldo auditável derivado. Em troca, consultas de saldo exigem agregação e a próxima implementação precisa definir cuidadosamente referências, status, timezone e transações atômicas. Otimizações como snapshots são possíveis sem alterar a fonte de verdade.
