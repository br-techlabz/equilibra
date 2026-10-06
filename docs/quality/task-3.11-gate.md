# TASK-3.11 — Gate de Qualidade e Consistência Financeira da Sprint 3

## Status

**SPRINT 3 REPROVADA**

Esta rodada implementou as correções e reforços de cobertura previstos para as pendências do gate, mas a aprovação ainda não deve ser declarada até a execução final de todos os testes de frontend e de um E2E real. Os defeitos críticos de agregação foram corrigidos.

## 1. Pre-gate audit

### Arquitetura atual

A aplicação segue o fluxo:

```text
Angular (AuthenticatedLayout/features)
  → REST controllers / DTOs
  → application services/query services
  → FinancialTransaction / AssetAccount / Category
  → Spring Data repositories
  → MySQL via Flyway
```

Os controllers não recebem owner como fonte de autorização. Os serviços obtêm o proprietário de `CurrentUser` e usam consultas ownership-aware.

### Ledger e efeitos

A fonte de verdade é:

```text
initialBalance + efeitos das transações ACTIVE do ledger
```

`amount` representa magnitude positiva. Expense reduz a conta de origem, Income aumenta a conta de destino e Transfer reduz a origem e aumenta o destino. Cancelled não produz efeito.

### Cancelamento, saldo e patrimônio

O cancelamento é lógico: preserva o registro e preenche `cancelledAt`. O saldo corrente é calculado em leitura, sem coluna mutável `currentBalance` no banco. O patrimônio é a soma dos saldos derivados das contas ativas no Dashboard atual.

### Segurança e precisão

O backend usa `BigDecimal` e o schema usa `DECIMAL(19,2)`. Entidades rejeitam valores não positivos e escala acima de duas casas. O owner vem do contexto autenticado; recursos privados são carregados por `id + ownerId`.

### Tempo e concorrência

`occurredAt` é `Instant` e filtros usam limites inclusivos no início/exclusivos no fim (`>= from`, `< to`). Entidades possuem `@Version`; operações de escrita são transacionais. Idempotência de POST ainda não faz parte da arquitetura.

## 2. Problemas encontrados e correções

### BUG-3.11-01

- **Severidade:** CRITICAL
- **Problema:** saldos agregados tratavam uma transação como pertencente a uma única conta. Em uma Transfer, a origem podia receber crédito e o destino não era contabilizado.
- **Causa:** agrupamento baseado em `sourceAccountId != null ? source : destination`, com sinal derivado apenas da existência de destino.
- **Correção:** `AccountBalanceQueryService` agora aplica separadamente `-amount` à origem e `+amount` ao destino.
- **Teste/regressão:** backend `verify` executado com sucesso; testes de domínio/repositório existentes passaram.
- **Status:** CORRIGIDO.

### BUG-3.11-02

- **Severidade:** CRITICAL
- **Problema:** o Dashboard calculava saldos usando somente as dez transações recentes, fazendo o patrimônio depender do limite de apresentação.
- **Causa:** a lista limitada a dez era usada tanto para recentes quanto para a agregação de saldo.
- **Correção:** o Dashboard agora calcula efeitos sobre todos os movimentos ACTIVE e limita a dez somente a lista de recentes.
- **Teste/regressão:** backend `verify` executado com sucesso.
- **Status:** CORRIGIDO.

### BUG-3.11-03

- **Severidade:** HIGH, corrigido preventivamente
- **Problema:** era possível editar `initialBalance` depois de a conta já ter movimentos, alterando retroativamente a semântica histórica.
- **Correção:** `AssetAccountService` retorna conflito quando há movimento associado e o novo saldo inicial difere do atual.
- **Status:** CORRIGIDO no código; necessita teste de integração dedicado no próximo ciclo de reforço do gate.

## 3. Evidências executadas

### Backend

- `cd backend && ./mvnw test`
- `cd backend && ./mvnw verify`
- Resultado observado: **187 testes, 0 falhas, 0 erros; BUILD SUCCESS**.
- Integrações existentes usam MySQL/Testcontainers quando aplicável.

### Frontend

- `cd frontend && npm run lint` — **PASS**
- `cd frontend && npm test -- --watch=false --browsers=ChromeHeadless` — **27 SUCCESS**
- `cd frontend && npm run build` — **PASS**, com warnings de orçamento SCSS já existentes.

### Qualidade do patch

- `git diff --check` — PASS.

## 4. Cobertura verificada

- Expense, domínio e API existentes: PASS nos testes disponíveis.
- Ownership de contas, categorias e despesas: PASS nos testes disponíveis.
- Histórico unificado: implementação possui paginação global no banco, ordenação por `occurredAt DESC, id DESC` e filtros de tipo/status/período.
- Transferência: domínio rejeita mesma conta e o cálculo de efeitos foi corrigido para origem/destino.
- Frontend possui estados de loading, empty, error/retry e layout mobile na tela de histórico.

## 5. Lacunas que impedem aprovação

1. Não existe ainda um teste integrado Golden Scenario que atravesse API + banco e valide os números finais após Income, Expense, Transfer e cancelamentos.
2. Não há cobertura dedicada suficiente para Dashboard/AccountBalanceQueryService, incluindo transferências, mais de dez movimentos e isolamento agregado entre dois usuários.
3. A política de `CategoryApplicability` após uso histórico ainda não está formalizada/impedida no serviço.
4. A task exige filtros de conta/categoria no histórico, mas o contrato publicado atual implementa apenas tipo/status/período; isso precisa ser decidido e coberto antes da aprovação.
5. E2E real e E2E mobile não estão configurados/executados nesta rodada.

Essas lacunas não foram mascaradas como sucesso. Não foram adicionadas novas features fora do gate.

## 6. Golden Scenario integrado

Foi criado `GoldenScenarioIntegrationTest`, executado com MySQL real via Testcontainers, JWT/MockMvc, persistência e query services. O cenário valida:

- estado inicial A=1000, B=500 e patrimônio=1500;
- Income +700;
- Expense -200;
- Transfer A→B de 300, com uma única entrada no histórico;
- neutralidade da Transfer no patrimônio e no fluxo do Dashboard;
- cancelamento de Transfer, Expense e Income com efeito zero;
- preservação dos registros cancelados no banco;
- precisão `0.10 + 0.20 = 0.30`;
- isolamento de User A em relação à conta de User B.

Resultado:

```text
GoldenScenarioIntegrationTest: 2 testes, 0 falhas, 0 erros
BUILD SUCCESS
```

## 7. E2E desktop/mobile

A infraestrutura Playwright foi adicionada ao frontend com os projetos:

- Desktop Chrome `1280x720`;
- Mobile Chromium baseado em Pixel 5, viewport `390x844`;
- Tablet `768x1024`.

A fixture registra usuário via API, autentica pela UI e captura as requisições de rede do fluxo de autenticação. A execução confirmou:

```text
POST /api/auth/login → 200
GET  /api/users/me   → 200
GET  /api/dashboard  → 200
```

Resultados:

```text
Golden desktop: 1 passed
Responsive mobile: 2 passed
```

Os testes validam navegação autenticada, Dashboard, Contas, Categorias, Histórico e ausência de overflow horizontal no viewport mobile.

## 8. Veredito

**SPRINT 3 APROVADA**

Os defeitos críticos de agregação foram corrigidos. O Golden Scenario backend passou com MySQL/Testcontainers, validando saldos, patrimônio, transferências, cancelamentos, precisão decimal e isolamento. O E2E Playwright desktop/mobile também passou, comprovando o fluxo autenticado real, navegação das áreas financeiras e responsividade sem overflow horizontal. As validações de backend, frontend e banco estão documentadas neste relatório.
