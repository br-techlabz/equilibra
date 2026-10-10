# TASK-7.3 — Backend de Compromissos Futuros

## Status

**CONCLUÍDA**

## Implementado

- ADR complementar 0007.
- Migration `V14__create_financial_commitments.sql`.
- Entidade `FinancialCommitment` com estados PENDING, SETTLED e CANCELLED.
- CRUD básico owner-scoped em `/commitments`.
- Criação manual de compromissos.
- Atualização limitada a compromissos pendentes.
- Cancelamento de compromissos pendentes.
- Efetivação de despesas e receitas via serviços existentes.
- Geração sob demanda com intervalo máximo de 12 meses.
- Derivação de `overdue` sem persistência.
- Constraint de ocorrência e vínculo único de transação.

## Verificação

- `./mvnw -q -Dtest=FinancialCommitmentTest test`: aprovado.
- `./mvnw test -q`: aprovado com regressão completa do backend.
- `./mvnw test -q -DskipTests`: compilação executada com sucesso.

## Validação final adicionada

- `FinancialCommitmentRollbackTest`: rollback de unidade transacional após persistência, aprovado.
- `FinancialCommitmentConcurrencyTest`: duas tentativas simultâneas sobre o mesmo agregado, apenas uma transição para SETTLED, aprovado.
- Geração idempotente, dia 31 e retry de efetivação: cenários integrados aprovados.

## Pendências remanescentes

Nenhuma pendência conhecida dentro do escopo da TASK-7.3.

As anotações OpenAPI dos endpoints foram adicionadas nesta etapa.

## Validação desta etapa

- `./mvnw -q -Dtest=FinancialCommitmentTest,FinancialCommitmentControllerIntegrationTest test`: aprovado.
- `./mvnw test -q`: aprovado.
- Filtros e paginação owner-scoped foram adicionados ao repository e controller.
- Testes de criação, listagem, filtro, cancelamento e isolamento entre usuários foram adicionados.
