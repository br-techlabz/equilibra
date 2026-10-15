# TASK-9.1 — PRE-IMPLEMENTATION REPORT: BACKEND DE RECONCILIAÇÃO DE SALDO

## Status

**PREPARED — pronto para implementação após aprovação do escopo.**

Este relatório atende à etapa obrigatória anterior à codificação da TASK-9.1. Nenhum código foi implementado nesta etapa.

## Contexto Inspecionado

Foram considerados:
- `CLAUDE.md` e regras de arquitetura, ownership, dinheiro (`BigDecimal`, `DECIMAL(19,2)`), tratamento de erros e segurança;
- `docs/adr/0012-balance-reconciliation-architecture.md` (ADR de Reconciliação de Saldo);
- `docs/adr/0002-financial-ledger-architecture.md` (Ledger e saldo derivado);
- `AssetAccount`, `AssetAccountRepository` e `AccountBalanceQueryService`;
- `FinancialTransaction`, `FinancialTransactionRepository`, `ExpenseService`, `IncomeService`;
- `Category`, `CategoryRepository`, `CategoryApplicability`;
- Contexto de segurança (`CurrentUser`, `SpringSecurityCurrentUser`, JWT);
- Tratamento de exceções (`ProblemDetail`, `ResourceNotFoundException`, `ResourceConflictException`).

## Objetivo da Implementação (TASK-9.1)

Implementar o backend completo para reconciliação de saldo de contas de ativo (`AssetAccount`), abrangendo:
1. **Cálculo de saldo histórico** em uma data específica (`balanceAt(accountId, targetDate)`).
2. **Provisionamento idempotente** da categoria "Ajuste de Saldo" (aplicabilidade `BOTH`).
3. **Serviço de Reconciliação** (`BalanceReconciliationService`) que calcula o saldo na data, compara com o saldo informado e gera automaticamente uma receita (`INCOME`) ou despesa (`EXPENSE`) com a descrição exata **"Ajuste de Saldo"** se houver diferença.
4. **Endpoint REST** seguro, ownership-aware, validando parâmetros monetários e datas, com tratamento adequado via Problem Details.
5. **Testes unitários e de integração** (Testcontainers/MySQL) cobrindo todos os cenários golden e corner cases solicitados.

## Especificação Técnica

### 1. Cálculo de Saldo Histórico
- **Data de Corte**: A data informada pelo usuário (ex: `LocalDate`) será convertida para o final do dia em UTC (`LocalDate.atTime(LocalTime.MAX)` ou equivalente `Instant` correspondente a `23:59:59.999999999` do dia no fuso UTC ou timezone da conta/sistema, conforme convenção do projeto). A convenção padrão do Equilibra para transações usa `Instant` em UTC.
- **Fórmula**:
  ```text
  balanceAt = initialBalance + somatório de efeitos das transações ACTIVE com occurredAt <= targetInstant vinculadas à conta.
  ```
- **Efeitos**:
  - `sourceAccountId == accountId` → subtrai `amount`.
  - `destinationAccountId == accountId` → adiciona `amount`.

### 2. Categoria "Ajuste de Saldo"
- Antes de criar a transação de ajuste, o sistema buscará por uma categoria ativa do owner com nome normalizado igual a `ajuste de saldo` (`normalizedName == "ajuste de saldo"`).
- Se não existir, criará uma nova categoria com nome `"Ajuste de Saldo"` e aplicabilidade `CategoryApplicability.BOTH`.
- Esta operação será idempotente e transacional.

### 3. Operação de Ajuste
- **Diferença** = `Saldo Informado` − `Saldo Calculado na Data`.
- Se `Diferença > 0`: Cria `INCOME` (Receita) via `IncomeService` ou repositório/transação, com:
  - `description` = `"Ajuste de Saldo"`
  - `amount` = `Diferença`
  - `occurredAt` = Data da reconciliação (fim do dia ou instante informado)
  - `destinationAccountId` = ID da conta
  - `categoryId` = ID da categoria "Ajuste de Saldo"
- Se `Diferença < 0`: Cria `EXPENSE` (Despesa) com:
  - `description` = `"Ajuste de Saldo"`
  - `amount` = `|Diferença|`
  - `occurredAt` = Data da reconciliação
  - `sourceAccountId` = ID da conta
  - `categoryId` = ID da categoria "Ajuste de Saldo"
- Se `Diferença == 0`: Nenhuma transação é criada.

### 4. Contrato REST Previsto
- **Endpoint**: `POST /api/accounts/{id}/reconcile` (ou conforme alinhamento REST do módulo account).
- **Request DTO** (`ReconcileAccountRequest`):
  - `reconciliationDate` (LocalDate, obrigatório)
  - `informedBalance` (BigDecimal, obrigatório, escala <= 2)
- **Response DTO** (`ReconciliationResultResponse`):
  - `accountId` (String/UUID)
  - `reconciliationDate` (LocalDate/Instant)
  - `calculatedBalance` (BigDecimal)
  - `informedBalance` (BigDecimal)
  - `difference` (BigDecimal)
  - `adjustmentType` (String: `"INCOME"`, `"EXPENSE"`, `"NONE"`)
  - `createdTransactionId` (String/UUID, opcional)

## Validações e Segurança
- Conta deve pertencer ao owner autenticado (`CurrentUser.id()`). Se não pertencer ou não existir, retornar `404 Not Found`.
- Conta deve estar ativa (`active == true`). Se inativa, retornar conflito (`409`).
- Validação estrita de `BigDecimal` com no máximo 2 casas decimais.
- Proteção contra concorrência via `@Transactional` e versionamento.

## Arquivos Previstos
1. `br.com.equilibra.account.api.ReconcileAccountRequest`
2. `br.com.equilibra.account.api.ReconciliationResultResponse`
3. `br.com.equilibra.account.api.AssetAccountController` (adicionar endpoint de reconciliação)
4. `br.com.equilibra.account.application.BalanceReconciliationService`
5. Testes unitários e de integração (`BalanceReconciliationServiceTest`, `AssetAccountReconciliationIntegrationTest`).

---
**READY FOR TASK-9.1 IMPLEMENTATION**
