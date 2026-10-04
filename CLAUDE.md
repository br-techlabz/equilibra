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

O backend utilizará **monólito modular organizado por domínio/feature**.

A organização deve evitar uma estrutura global baseada apenas em camadas como `controller/`, `service/`, `repository/` e `entity/` na raiz da aplicação.

### Package raiz

O package raiz do backend é:

```text
br.com.equilibra
```

A classe principal `EquilibraApiApplication` deve permanecer nesse nível para permitir component scanning dos módulos atuais e futuros.

### Domínios previstos

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

Não criar packages vazios apenas para representar módulos futuros. Crie somente packages/classes que tenham função concreta na tarefa atual.

### Estrutura interna preferencial dos módulos

Quando um módulo for implementado, utilize preferencialmente a organização:

```text
<domain>/
├── api/
├── application/
├── domain/
└── infrastructure/
```

Responsabilidades:

- `api`: controllers, request DTOs, response DTOs e contratos REST do módulo. Não deve conter regras de negócio.
- `application`: casos de uso, serviços de aplicação, coordenação de operações e transações quando apropriado.
- `domain`: entidades, enums, regras de negócio e conceitos do domínio. Não deve depender da camada HTTP.
- `infrastructure`: repositories, persistência, adapters, integrações externas e implementações técnicas.

Não aplicar essa estrutura mecanicamente quando ela não trouxer benefício. O Equilibra deve continuar sendo um monólito modular pragmático.

### Dependências entre módulos

1. Um módulo não deve acessar diretamente detalhes internos de outro módulo.
2. Controllers devem acessar casos de uso/serviços de aplicação, nunca repositories diretamente.
3. Repositories não devem ser utilizados diretamente por controllers.
4. DTOs da API não devem ser usados como entidades JPA.
5. Entidades JPA nunca devem ser retornadas diretamente pelos controllers.
6. Módulos devem expor apenas o necessário para colaboração com outros módulos.
7. `shared` deverá conter somente elementos realmente compartilhados.
8. Não transformar `shared` em um local genérico para qualquer código sem classificação.
9. Evitar dependências circulares.
10. Não criar abstrações antecipadamente sem necessidade concreta.

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

## Regras de Segurança (Senhas)

15. **Senha nunca pode ser logada** (raw, hash ou qualquer derivada).
16. **passwordHash nunca pode ser retornado pela API** (DTOs, responses, toString).
17. **PasswordEncoder é obrigatório** para toda operação de hash/verificação.
18. **Algoritmo próprio é proibido** (não usar SHA-256, MD5, Base64, AES para senhas).
19. **Comparação de senha exclusivamente via `PasswordEncoder.matches()`**.
20. **Não aplicar `trim()` silenciosamente** em senhas antes de hash.
21. **JWT nunca pode ser logado** completo ou parcialmente como credencial.
22. **Secrets/chaves de JWT nunca devem ser versionados**; use configuração externa.
23. **JWT não deve transportar dados financeiros, passwordHash ou dados sensíveis desnecessários**.
24. **APIs de negócio devem ser protegidas por padrão**, liberando publicamente apenas endpoints explicitamente aprovados.

---

## Regras de API e DTOs

1. APIs REST deverão utilizar DTOs específicos para entrada e saída.
2. Request e Response devem ser separados quando seus objetivos forem diferentes.
3. Nunca utilizar entidades JPA como payload público da API.
4. Entidades JPA não devem ser expostas diretamente por controllers.
5. MapStruct poderá ser utilizado quando houver mapeamentos reais e ele reduzir código repetitivo.
6. Não adicionar MapStruct por antecipação se não houver mapeamento real na tarefa atual.
7. Evitar mapeadores excessivamente genéricos.

---

## Identificadores

1. Para novas entidades de negócio, prefira UUID, salvo decisão arquitetural documentada diferente.
2. A representação externa dos IDs deverá ser consistente nas APIs.
3. Não criar entidades apenas para testar a estratégia de UUID.

---

## Datas, horários e timezone

1. Para eventos técnicos como `createdAt`, `updatedAt`, `loginAt` e timestamps de auditoria, prefira `Instant`.
2. Para data/hora informada pelo usuário em transações financeiras, a modelagem deverá considerar explicitamente timezone.
3. Não tomar decisões implícitas baseadas no timezone do servidor.
4. A estratégia de timezone deverá ser explicitada antes da implementação de transações financeiras.

---

## Valores monetários

1. Valores financeiros devem utilizar `BigDecimal` no backend.
2. Nunca utilizar `float`, `Float`, `double` ou `Double` para valores monetários.
3. Precisão e escala deverão ser definidas explicitamente nas entidades financeiras futuras.
4. Regras financeiras permanecem no backend; o frontend não deve implementá-las.

---

## Paginação, ordenação e filtros

1. APIs que retornarem coleções potencialmente grandes deverão possuir paginação server-side.
2. Utilize mecanismos do Spring Data quando apropriado.
3. Não retornar listas ilimitadas de transações, auditoria, anexos ou relatórios detalhados.
4. Filtros sobre conjuntos potencialmente grandes devem ser executados preferencialmente no backend.
5. Não carregar milhares de registros no Angular para realizar filtragem local.
6. Parâmetros de ordenação e filtro deverão ser validados.

---

## Transações de banco

1. Utilize `@Transactional` na camada de aplicação/service quando uma operação representar uma unidade atômica de negócio.
2. Evite colocar `@Transactional` em controllers.
3. Transferências financeiras futuras deverão obrigatoriamente ser atômicas.

---

## Segurança e contexto do usuário

1. Nenhum endpoint futuro deverá aceitar `userId` fornecido pelo frontend como fonte de autorização ou propriedade.
2. O usuário deverá ser identificado pelo contexto autenticado do Spring Security.
3. É proibido confiar em parâmetros como `POST /transactions?userId=123` para determinar o proprietário do dado.
4. É proibido confiar em payloads como `{ "userId": "123" }` para autorização ou isolamento de dados.
5. Repositories e services deverão garantir isolamento por usuário quando apropriado.
6. A implementação efetiva da autenticação ocorrerá na Sprint correspondente.
7. O frontend nunca será considerado uma barreira de segurança.

---

## Auditoria

1. O sistema deverá conseguir registrar futuramente eventos relevantes de criação, alteração, exclusão, login e operações financeiras importantes.
2. Não adicionar frameworks de auditoria sem necessidade concreta.
3. O módulo de auditoria será implementado em tarefa/sprint própria.

---

## Logging

1. Utilizar SLF4J.
2. Evitar `System.out.println`.
3. Não registrar senhas.
4. Não registrar tokens JWT.
5. Não registrar conteúdo sensível de anexos.
6. Evitar dados financeiros desnecessários nos logs.
7. Utilizar níveis adequados de log.

---

## Exceções e contrato REST

1. O backend deverá possuir estratégia centralizada de tratamento de erros.
2. O contrato de erros deve utilizar `ProblemDetail`/Problem Details quando apropriado.
3. Não criar envelopes globais obrigatórios como `success/data/error`.
4. Não criar mecanismos concorrentes de erro sem necessidade.
5. Erros inesperados não devem expor stack trace, SQL, paths internos, credenciais ou detalhes sensíveis.
6. Validação de campos deve retornar estrutura consumível pelo frontend com campo e mensagem.
7. Toda resposta HTTP deverá possuir correlação por `X-Request-ID` quando passar pela infraestrutura web.
8. O request ID deverá ser incluído no MDC durante a requisição e removido ao final.
9. Convenções REST estão documentadas em `docs/api-guidelines.md`.

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

## Configuração e segredos

1. Configurações específicas de ambiente deverão permanecer externas ao código sempre que apropriado.
2. Segredos nunca devem ser versionados.
3. Preserve a estratégia de `application.yml`, profiles e variáveis de ambiente.
4. `.env` real deve permanecer ignorado pelo Git.
5. `.env.example` pode conter apenas valores de desenvolvimento/exemplo.

---

## Testabilidade

1. A arquitetura deverá facilitar testes unitários e de integração.
2. Regras de negócio importantes devem poder ser testadas sem necessidade de iniciar todo o servidor HTTP.
3. Integrações com banco deverão utilizar Testcontainers quando apropriado.
4. Não utilizar H2 para substituir comportamentos específicos do MySQL.

---

## Documentação arquitetural

1. A arquitetura base está documentada em `docs/architecture.md`.
2. A arquitetura frontend está documentada em `docs/frontend-architecture.md`.
3. Decisões arquiteturais importantes poderão ser registradas em ADRs dentro de `docs/adr/`.
4. O ADR inicial do monólito modular é `docs/adr/0001-modular-monolith.md`.

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
