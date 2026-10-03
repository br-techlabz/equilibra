# CLAUDE.md — Equilibra

## Projeto

**Nome:** Equilibra

**Objetivo:**  
Aplicação web multiusuário para gerenciamento de finanças domésticas.

---

## Stack

### Backend
- Java 21 ou versão LTS compatível com Spring Boot 4
- Spring Boot 4
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- MySQL 8
- Flyway
- OpenAPI
- JUnit 5
- Mockito
- Testcontainers

### Frontend
- Angular 19
- TypeScript
- Angular Material
- SCSS
- Standalone Components
- Signals quando apropriado
- Reactive Forms
- Lazy Loading
- Playwright

### Infraestrutura
- Docker
- Docker Compose
- GitHub Actions

---

## Arquitetura

O backend utilizará **monólito modular organizado por domínio**.

### Domínios previstos:
- auth
- user
- account
- category
- tag
- transaction
- transfer
- attachment
- dashboard
- report
- audit
- shared

> Não criar esses módulos funcionalmente na TASK-0.1.

---

## Regras obrigatórias

1. Não implementar funcionalidades fora do escopo da tarefa atual.
2. Não fazer refatorações não relacionadas sem justificativa.
3. Não expor entidades JPA diretamente na API.
4. Controllers não devem conter regras de negócio.
5. O frontend não deve implementar regras financeiras.
6. Valores monetários deverão utilizar `BigDecimal` no backend.
7. Migrations deverão ser feitas exclusivamente pelo Flyway.
8. Não utilizar `ddl-auto=create`, `ddl-auto=create-drop` ou `ddl-auto=update`.
9. O backend será responsável pela autorização e isolamento dos dados dos usuários.
10. O frontend nunca será considerado uma barreira de segurança.
11. Não adicionar dependências sem necessidade.
12. Toda alteração deverá preservar os testes existentes.
13. Toda tarefa deverá terminar com build e testes relevantes.
14. Segredos, senhas e tokens nunca deverão ser versionados.

---

## Regras de migrations

1. Toda alteração estrutural no banco deverá ser feita por Flyway.
2. Nunca modificar uma migration que já tenha sido aplicada em ambientes compartilhados.
3. Correções de schema deverão ser realizadas através de uma nova migration.
4. Migrations deverão ser versionadas no Git.
5. Hibernate não deverá criar ou alterar tabelas automaticamente.
6. Migrations devem ser determinísticas.
7. Não inserir dados de usuário ou dados sensíveis através de migrations.
8. Dados iniciais necessários ao sistema deverão ser tratados explicitamente e separados das alterações estruturais quando apropriado.
9. Alterações destrutivas deverão ser avaliadas cuidadosamente antes da implementação.
10. Toda migration deverá ser compatível com MySQL.
11. Scripts de migration deverão ficar em `backend/src/main/resources/db/migration`.
12. O padrão obrigatório de nomenclatura é `V<versão>__<descricao>.sql`, por exemplo `V1__initial_schema.sql`.

---

## Fluxo para cada tarefa

### Antes de implementar:
1. Ler `CLAUDE.md`.
2. Analisar o estado atual do projeto.
3. Identificar arquivos relacionados.
4. Apresentar um plano resumido.
5. Implementar somente o escopo solicitado.

### Depois de implementar:
1. Executar testes.
2. Executar build.
3. Corrigir problemas introduzidos.
4. Informar arquivos criados.
5. Informar arquivos modificados.
6. Informar testes executados.
7. Informar eventuais pendências.