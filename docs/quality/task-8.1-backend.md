# TASK-8.1 — Backend de Cadastro de Cartões de Crédito

## Status

**CONCLUÍDA — backend implementado e validado.**

## Implementado

- Agregado `CreditCard` independente de `AssetAccount`.
- Estados `ACTIVE` e `INACTIVE`.
- Campos owner-scoped: nome, instituição, apelido, limite, fechamento, vencimento, timezone e timestamps.
- Validação de limite positivo com escala máxima de duas casas.
- Validação de dias entre 1 e 31.
- Validação de timezone IANA, com default `America/Sao_Paulo`.
- CRUD REST protegido por autenticação:
  - `POST /credit-cards`;
  - `GET /credit-cards`;
  - `GET /credit-cards/{id}`;
  - `PUT /credit-cards/{id}`;
  - `PATCH /credit-cards/{id}/deactivate`;
  - `PATCH /credit-cards/{id}/activate`.
- Ownership derivado exclusivamente de `CurrentUser.id()`.
- IDs inválidos ou pertencentes a outro usuário tratados como cartão inexistente.
- Indicadores retornados sem consumo fictício:
  - `committedLimit = 0.00`;
  - `reservedLimit = 0.00`;
  - `availableLimit = totalLimit`.
- Desativação lógica, sem exclusão física.
- Nenhuma alteração em `FinancialTransaction`, `ExpenseService` ou `AccountBalanceQueryService`.
- Nenhum número de cartão, CVV, token ou credencial financeira armazenado.

## Migration

Criada:

```text
backend/src/main/resources/db/migration/V15__create_credit_cards.sql
```

A migration contém:

- tabela `credit_cards` em InnoDB;
- FK para `users`;
- `DECIMAL(19,2)`;
- checks de limite, dias e status;
- timestamps e versão otimista;
- índices owner-scoped por status e nome.

## Arquivos criados

```text
backend/src/main/java/br/com/equilibra/creditcard/domain/CreditCard.java
backend/src/main/java/br/com/equilibra/creditcard/domain/CreditCardStatus.java
backend/src/main/java/br/com/equilibra/creditcard/api/CreditCardController.java
backend/src/main/java/br/com/equilibra/creditcard/api/CreditCardResponse.java
backend/src/main/java/br/com/equilibra/creditcard/api/CreateCreditCardRequest.java
backend/src/main/java/br/com/equilibra/creditcard/api/UpdateCreditCardRequest.java
backend/src/main/java/br/com/equilibra/creditcard/application/CreditCardService.java
backend/src/main/java/br/com/equilibra/creditcard/infrastructure/CreditCardRepository.java
backend/src/main/resources/db/migration/V15__create_credit_cards.sql
```

## Golden scenario

Para cartão com limite de `5.000,00`, fechamento dia `3` e vencimento dia `10`, sem compras:

- limite disponível retorna `5.000,00`;
- limite comprometido retorna `0,00`;
- limite reservado retorna `0,00`;
- nenhuma despesa, compromisso ou transação é criada;
- o saldo bancário não é alterado.

## Validação executada

```bash
cd backend
./mvnw test -q
./mvnw package -q -DskipTests
```

Resultados:

- testes backend: aprovados;
- package/build: aprovado;
- regressões existentes: preservadas.

## Fora do escopo

- compras;
- parcelamentos;
- faturas;
- pagamentos;
- integração frontend;
- consumo de limite real;
- saldo devedor inicial operacional.

## Riscos e pendências

- Os indicadores de limite permanecem zerados até a implementação de compras e reservas nas TASKS seguintes.
- A normalização de dias inexistentes em meses curtos pertence à implementação de faturas da TASK-8.3.
- Testes específicos adicionais de integração do módulo poderão ser ampliados junto ao ambiente Testcontainers já utilizado pelas regressões existentes.

**READY FOR TASK-8.2.**
