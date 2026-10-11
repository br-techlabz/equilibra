# TASK-8.1 — PRE-IMPLEMENTATION REPORT

## Status

**PREPARED — pronto para implementação após aprovação do escopo.**

Este relatório atende à etapa obrigatória anterior à codificação da TASK-8.1. Nenhum código foi implementado nesta etapa.

## Contexto inspecionado

Foram considerados:

- `CLAUDE.md` e regras de arquitetura, segurança, ownership, dinheiro e Flyway;
- `docs/adr/0011-credit-card-architecture.md`;
- `docs/adr/0002-financial-ledger-architecture.md`;
- `docs/quality/task-7.8-sprint-7-gate.md`;
- `AssetAccount` e seus endpoints;
- `FinancialTransaction` e `ExpenseService`;
- `CurrentUser` e autenticação JWT;
- migrations existentes, especialmente contas e transações;
- padrões de DTO, validação, `ProblemDetail`, OpenAPI e Request ID;
- padrões de testes unitários e Testcontainers/MySQL.

A Sprint 7 está aprovada e o ADR 0011 encerra com `READY FOR TASK-8.1`.

## Objetivo da implementação

Criar o agregado privado `CreditCard` e seu CRUD owner-scoped, sem implementar compras, parcelamentos, faturas ou pagamentos.

O cartão será uma entidade de crédito independente de `AssetAccount`. O cadastro não deverá criar `FinancialTransaction`, despesa, compromisso, movimento bancário ou alteração de saldo.

## Decisões de domínio aplicáveis

### Entidade

Nome escolhido: `CreditCard`.

Campos previstos:

| Campo | Tipo | Regra |
|---|---|---|
| `id` | UUID string | gerado pelo backend, imutável |
| `ownerId` | UUID string | derivado exclusivamente de `CurrentUser.id()` |
| `name` | texto | obrigatório, nome exibido |
| `institution` | texto | obrigatório, emissor |
| `nickname` | texto anulável | apelido opcional |
| `totalLimit` | `BigDecimal` | positivo, escala máxima 2 |
| `closingDay` | inteiro | 1 a 31 |
| `dueDay` | inteiro | 1 a 31 |
| `timezone` | IANA string | default explícito `America/Sao_Paulo` |
| `status` | enum | `ACTIVE`, `INACTIVE` |
| `createdAt` | `Instant` | timestamp técnico |
| `updatedAt` | `Instant` | timestamp técnico |
| `version` | `Long` | `@Version` para concorrência |

Não serão armazenados número completo, CVV, senha, token ou credenciais financeiras. Últimos quatro dígitos não fazem parte desta task porque não são necessários para o cadastro mínimo aprovado.

### Limites

Nesta task, sem compras ou reservas, os indicadores serão derivados de forma autoritativa:

```text
committedLimit = 0.00
reservedLimit = 0.00
availableLimit = totalLimit
```

A API não deve inventar consumo. A estrutura de resposta poderá expor os três indicadores para compatibilidade com o domínio futuro, mas deve deixar claro que o consumo ainda é zero porque as compras não fazem parte da TASK-8.1.

Saldo inicial de obrigação não será implementado nesta task, pois o ADR prevê uma operação explícita de abertura e não um campo livre no CRUD. Se o contrato da implementação exigir suporte imediato, a tarefa deverá ser bloqueada para decisão; a recomendação é manter fora do escopo.

### Datas

`closingDay` e `dueDay` são regras civis recorrentes, não datas completas. Dias 29, 30 e 31 serão aceitos porque a normalização para meses curtos pertence ao calendário de faturas da TASK-8.3.

A consistência mínima desta task será:

- ambos entre 1 e 31;
- ambos obrigatórios;
- não exigir que `dueDay` seja maior que `closingDay`, pois o vencimento pode ocorrer no mês seguinte e o ADR não define essa comparação como regra válida.

Timezone será validado como zone ID IANA por `ZoneId.of`, sem usar timezone do servidor.

### Status e histórico

- Cadastro inicia em `ACTIVE`.
- `PATCH /credit-cards/{id}/deactivate` leva a `INACTIVE`.
- Cartão inativo continua consultável.
- Não haverá exclusão física nesta task.
- Reativação poderá seguir o padrão existente de contas (`PATCH /{id}/activate`) caso seja necessária para simetria; recomenda-se incluí-la no contrato por ser operação reversível e já existir como convenção no módulo de contas.
- Como ainda não há compras, não existe dependência de histórico que impeça a desativação.

## Contratos REST previstos

Base recomendada: `/credit-cards`, alinhada ao padrão do módulo de contas.

### Criar

```http
POST /credit-cards
Authorization: Bearer <token>
Content-Type: application/json
```

Request conceitual:

```json
{
  "name": "Cartão Principal",
  "institution": "Banco Exemplo",
  "nickname": "Meu cartão",
  "totalLimit": 5000.00,
  "closingDay": 3,
  "dueDay": 10,
  "timezone": "America/Sao_Paulo"
}
```

Regras:

- não aceitar `ownerId`;
- responder `201 Created`;
- incluir `Location: /credit-cards/{id}`;
- não criar movimento financeiro.

### Listar

```http
GET /credit-cards?includeInactive=false
```

Como o CRUD de cartões pode crescer, o service deverá limitar tamanho se futuramente forem adicionados filtros/paginação. Para a primeira implementação, a lista pode seguir o padrão existente do módulo de contas; a decisão deverá ser registrada no código e coberta por teste.

### Detalhar

```http
GET /credit-cards/{id}
```

O ID deve ser validado. Cartão inexistente ou pertencente a outro usuário deve resultar em `404` indistinguível.

### Atualizar

```http
PUT /credit-cards/{id}
```

Campos editáveis:

- nome;
- instituição;
- apelido;
- limite total;
- dia de fechamento;
- dia de vencimento;
- timezone.

Não aceitar alteração de `id`, `ownerId`, `status`, timestamps ou `version` no payload. Como compras ainda não existem, não há bloqueio de alteração por histórico nesta task; a camada de aplicação deverá, entretanto, manter a regra futura de impedir alterações incompatíveis quando houver obrigação.

### Desativar e reativar

```http
PATCH /credit-cards/{id}/deactivate
PATCH /credit-cards/{id}/activate
```

Operações owner-scoped e idempotência compatível com o padrão de `AssetAccount`.

## Resposta conceitual

```json
{
  "id": "uuid",
  "name": "Cartão Principal",
  "institution": "Banco Exemplo",
  "nickname": "Meu cartão",
  "totalLimit": 5000.00,
  "committedLimit": 0.00,
  "reservedLimit": 0.00,
  "availableLimit": 5000.00,
  "closingDay": 3,
  "dueDay": 10,
  "timezone": "America/Sao_Paulo",
  "status": "ACTIVE",
  "createdAt": "2026-10-11T12:00:00Z",
  "updatedAt": "2026-10-11T12:00:00Z"
}
```

A entidade JPA não será retornada diretamente pelo controller. `passwordHash`, tokens e credenciais não possuem lugar no DTO.

## Arquivos previstos

### Criar

```text
backend/src/main/java/br/com/equilibra/creditcard/domain/CreditCard.java
backend/src/main/java/br/com/equilibra/creditcard/domain/CreditCardStatus.java
backend/src/main/java/br/com/equilibra/creditcard/api/CreditCardController.java
backend/src/main/java/br/com/equilibra/creditcard/api/CreateCreditCardRequest.java
backend/src/main/java/br/com/equilibra/creditcard/api/UpdateCreditCardRequest.java
backend/src/main/java/br/com/equilibra/creditcard/api/CreditCardResponse.java
backend/src/main/java/br/com/equilibra/creditcard/application/CreditCardService.java
backend/src/main/java/br/com/equilibra/creditcard/infrastructure/CreditCardRepository.java
backend/src/main/resources/db/migration/V15__create_credit_cards.sql
```

O número da migration deve ser confirmado contra o estado real no momento da implementação para evitar colisão com arquivos não observados ou alterações paralelas.

### Testes previstos

```text
backend/src/test/java/br/com/equilibra/creditcard/domain/CreditCardTest.java
backend/src/test/java/br/com/equilibra/creditcard/application/CreditCardServiceTest.java
backend/src/test/java/br/com/equilibra/creditcard/CreditCardIntegrationTest.java
```

Os nomes podem seguir a convenção efetivamente existente no projeto, preservando o isolamento do módulo.

### Não modificar

Não devem ser alterados nesta task:

- `FinancialTransaction`;
- `ExpenseService`;
- `AccountBalanceQueryService`;
- `AssetAccount`;
- migrations existentes;
- frontend;
- compras, parcelas, faturas ou pagamentos.

Integrações futuras deverão consultar cartões por módulo, sem fazer o ledger tratar cartão como conta bancária.

## Migration prevista

Tabela conceitual `credit_cards`:

- `id CHAR(36) PRIMARY KEY`;
- `owner_id CHAR(36) NOT NULL` com FK para `users(id)`;
- `name VARCHAR(...) NOT NULL`;
- `institution VARCHAR(...) NOT NULL`;
- `nickname VARCHAR(...) NULL`;
- `total_limit DECIMAL(19,2) NOT NULL`;
- `closing_day TINYINT NOT NULL`;
- `due_day TINYINT NOT NULL`;
- `timezone VARCHAR(...) NOT NULL`;
- `status VARCHAR(20) NOT NULL`;
- `created_at TIMESTAMP(6) NOT NULL`;
- `updated_at TIMESTAMP(6) NOT NULL`;
- `version BIGINT NOT NULL DEFAULT 0`.

Constraints:

- limite maior que zero;
- escala monetária `DECIMAL(19,2)`;
- dias entre 1 e 31;
- status apenas `ACTIVE` ou `INACTIVE`;
- FK de owner;
- índices `(owner_id, status)` e `(owner_id, name)` ou equivalente para listagem owner-scoped.

Não criar constraint de unicidade global de nome, instituição ou apelido: usuários podem possuir cartões com os mesmos dados e o isolamento é por owner.

## Validações

### Entrada

- `name`: obrigatório, trim conforme padrão de nomes, tamanho máximo definido explicitamente no DTO/domínio;
- `institution`: obrigatório e limitado;
- `nickname`: opcional, normalizado sem transformar ausência em valor inválido;
- `totalLimit`: obrigatório, positivo, escala máxima 2;
- `closingDay`: inteiro entre 1 e 31;
- `dueDay`: inteiro entre 1 e 31;
- `timezone`: zone ID IANA válido.

Não aplicar `trim()` em senha porque não há senha neste domínio. Campos textuais comuns podem seguir o padrão já usado por `AssetAccount`.

### Negócio

- owner sempre do contexto autenticado;
- cartão inativo não aceita novas operações financeiras futuras;
- cadastro não gera efeito no saldo;
- `availableLimit` não pode ser negativo;
- limite não pode ser zero ou negativo;
- IDs de cartão de outro owner são tratados como inexistentes.

## Segurança e erros

- Controller protegido por autenticação JWT e `@SecurityRequirement`.
- Service utiliza `CurrentUser.id()`.
- Repository expõe consultas owner-aware (`findByIdAndOwnerId`, `findAllByOwnerId...`).
- Não aceitar owner no request, path ou query.
- Validação retorna `ProblemDetail` com campos conforme `GlobalExceptionHandler`.
- UUID inválido e recurso inexistente devem seguir o contrato existente sem revelar dados.
- Request ID será provido pelo `RequestIdFilter`; nenhum endpoint novo deverá criar mecanismo paralelo.
- Não haverá logging de dados sensíveis ou credenciais.

## Testes previstos

### Domínio

- cria cartão válido;
- rejeita nome/instituição ausentes;
- rejeita limite zero, negativo ou com mais de duas casas;
- aceita limite com escala menor e normaliza para duas casas;
- rejeita fechamento fora de 1–31;
- rejeita vencimento fora de 1–31;
- aceita dias 29, 30 e 31;
- valida timezone IANA;
- inicia ativo;
- desativa e reativa;
- preserva owner e ID.

### Aplicação/API

- cria cartão para usuário autenticado;
- não aceita `ownerId` como fonte de propriedade;
- lista apenas cartões do usuário atual;
- detalha cartão próprio;
- cartão de outro usuário retorna 404;
- atualiza cartão próprio;
- desativa cartão próprio;
- não exclui fisicamente cartão;
- resposta contém limite total e indicadores zerados;
- ausência de compras não cria transação, despesa, compromisso ou movimento.

### Integração MySQL/Testcontainers

- migration cria tabela e constraints;
- persistência de UUID, owner, valores e timestamps;
- FK para usuário;
- checks de limite, dias e status;
- isolamento entre dois usuários;
- concorrência de atualização detecta `@Version` quando aplicável;
- regressão de contas e transações confirma saldo bancário inalterado.

## Golden scenario da TASK-8.1

Entrada:

```text
limite: 5.000,00
fechamento: dia 3
vencimento: dia 10
```

Resultado esperado:

- cartão criado com status `ACTIVE`;
- limite comprometido `0,00`;
- limite reservado `0,00`;
- limite disponível `5.000,00`;
- nenhuma `FinancialTransaction` criada;
- nenhuma despesa criada;
- nenhum `FinancialCommitment` criado;
- nenhuma saída ou entrada em `AssetAccount`;
- saldo da conta bancária existente inalterado.

## Riscos e mitigação

- **Colisão de migration:** confirmar o próximo número Flyway antes de criar o arquivo.
- **Cartão tratado como conta:** manter módulo separado e não alterar `FinancialTransaction`/`AccountBalanceQueryService`.
- **Indicador fictício:** retornar zero apenas porque não há compras persistidas; não criar consumo artificial.
- **Timezone inválido:** validar com `ZoneId.of` e testar IDs inválidos.
- **Vazamento entre owners:** queries sempre combinam ID e owner.
- **Alteração concorrente:** usar `@Version` e mapear conflito para `ProblemDetail`/conflito existente.
- **Incompatibilidade futura do limite:** manter `committedLimit` e `reservedLimit` como resultado derivado, sem persistir contadores mutáveis nesta task.

## Ordem de implementação prevista

1. Confirmar migration mais recente e padrões de testes.
2. Criar enum e entidade com invariantes de domínio.
3. Criar repository owner-aware.
4. Criar requests/responses e validações.
5. Criar service transacional com `CurrentUser`.
6. Criar controller, OpenAPI e status HTTP.
7. Criar migration Flyway.
8. Criar testes unitários.
9. Criar testes Testcontainers/MySQL e regressões.
10. Executar build, testes backend e validar o golden scenario.
11. Produzir relatório final da TASK-8.1.

## Gate pré-implementação

A arquitetura, contratos e arquivos previstos estão definidos. Não há decisão essencial pendente para o cadastro básico de cartões.

**READY TO IMPLEMENT TASK-8.1.**
