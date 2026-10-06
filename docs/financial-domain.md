# Domínio financeiro — Sprint 3

Este documento resume as regras decididas na ADR 0002 e orienta a implementação futura do ledger. A TASK-3.0 não cria entidades, migrations, endpoints ou telas.

## Fonte de verdade

O saldo não será armazenado como campo mutável em `AssetAccount`. A fonte de verdade será:

```text
initialBalance + transações ACTIVE do ledger
```

Para a conta `A` no instante `T`:

```text
balanceAt(A, T) = initialBalance(A)
  + INCOME recebidas até T
  - EXPENSE financiadas por A até T
  + TRANSFER recebidas por A até T
  - TRANSFER enviadas por A até T
```

Transações `CANCELLED` não participam. `occurredAt` é o instante financeiro; `createdAt` é o instante técnico de registro. Ambos não devem ser confundidos.

## FinancialTransaction

A Sprint 3 deverá usar um agregado conceitual unificado, com `TransactionType` (`EXPENSE`, `INCOME`, `TRANSFER`) e `TransactionStatus` (`ACTIVE`, `CANCELLED`). Despesas, receitas e transferências terão casos de uso explícitos, mas compartilharão histórico, ownership, auditoria e consultas temporais.

Campos conceituais: UUID, `ownerId`, type, status, description, occurredAt, amount positivo, categoryId, sourceAccountId, destinationAccountId, notes, createdAt, updatedAt, cancelledAt e versão otimista.

## Invariantes

| Tipo | Conta de origem | Conta de destino | Categoria | Efeito |
|---|---|---|---|---|
| EXPENSE | obrigatória e ativa ao criar | proibida | EXPENSE ou BOTH, ativa ao criar | origem − amount |
| INCOME | proibida | obrigatória e ativa ao criar | INCOME ou BOTH, ativa ao criar | destino + amount |
| TRANSFER | obrigatória e ativa ao criar | obrigatória, ativa, diferente da origem | BOTH recomendado | origem − amount; destino + amount |

Toda referência deve pertencer ao mesmo usuário da transação. `amount` é sempre positivo, não zero, com no máximo duas casas e `BigDecimal`/DECIMAL. BRL é implícito nesta versão; não há multi-moeda.

Transferências não são receitas ou despesas e não alteram patrimônio total entre contas do mesmo usuário. Transferências devem ser atômicas.

## Ownership e histórico

O owner vem exclusivamente de `CurrentUser.id()`. Conta/categoria de outro usuário nunca pode ser associada. Conta ou categoria desativada não pode ser escolhida em novo lançamento, mas o histórico antigo permanece válido.

Não haverá DELETE físico como operação principal. Cancelamento preserva o registro, preenche `cancelledAt`, deixa de afetar saldo e é preferencialmente terminal; uma correção futura deve ser um novo lançamento, não reativação silenciosa.

## Políticas adiadas

- Depois da primeira transação, edição direta de `initialBalance` deve ser bloqueada ou substituída por ajuste explícito/evento do ledger.
- Alteração de `CategoryApplicability` incompatível com histórico deve ser bloqueada ou gerar nova categoria.
- Tags, anexos, recorrência, multi-moeda, budgets, snapshots, reconciliação, importação bancária, parcelamento e cartão permanecem fora do escopo.
- “Contas de Despesas” ainda precisa de definição; não é automaticamente `AssetAccount`.
