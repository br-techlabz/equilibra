# Arquitetura do Equilibra

## Visão geral

O Equilibra é uma aplicação web multiusuário para gerenciamento de finanças domésticas.

O backend será desenvolvido como um **monólito modular organizado por domínio/feature**. A aplicação roda em um único processo, mas as fronteiras internas dos módulos devem ser preservadas para permitir evolução incremental sem transformar o código em uma estrutura global genérica por camadas.

Não há objetivo obrigatório de migração futura para microserviços.

## Stack efetivamente existente

### Backend

- Java 21
- Spring Boot 4
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- MySQL 8
- Flyway
- SpringDoc OpenAPI/Swagger
- Spring Boot Actuator
- JUnit 5
- Mockito
- Testcontainers

### Infraestrutura local

- Docker
- Docker Compose
- MySQL 8 via imagem oficial

### Frontend

O frontend Angular ainda não foi inicializado neste ponto do projeto.

## Package raiz

O package raiz do backend é:

```text
br.com.equilibra
```

A classe principal Spring Boot está em:

```text
backend/src/main/java/br/com/equilibra/EquilibraApiApplication.java
```

Ela deve permanecer em nível adequado para component scanning dos módulos atuais e futuros.

## Organização dos módulos

Módulos previstos:

- `auth`
- `user`
- `account`
- `category`
- `tag`
- `transaction`
- `transfer`
- `attachment`
- `dashboard`
- `report`
- `audit`
- `shared`

Packages vazios não devem ser criados apenas para representar módulos futuros. Cada package/classe deve ter função concreta na tarefa em que for criado.

Quando um módulo for implementado, a estrutura interna preferencial é:

```text
<domain>/
├── api/
├── application/
├── domain/
└── infrastructure/
```

Responsabilidades:

- `api`: controllers, DTOs de request/response e contratos REST do módulo.
- `application`: casos de uso, serviços de aplicação, coordenação de operações e transações quando apropriado.
- `domain`: entidades, enums, regras de negócio e conceitos do domínio.
- `infrastructure`: repositories, persistência, adapters, integrações externas e implementações técnicas.

Essa estrutura não deve ser aplicada mecanicamente se não trouxer benefício real.

## Dependências e fronteiras

Regras de dependência:

1. Um módulo não deve acessar diretamente detalhes internos de outro módulo.
2. Controllers devem acessar casos de uso/serviços de aplicação, nunca repositories diretamente.
3. Repositories não devem ser utilizados diretamente por controllers.
4. DTOs da API não devem ser usados como entidades JPA.
5. Entidades JPA nunca devem ser retornadas diretamente pelos controllers.
6. Módulos devem expor apenas o necessário para colaboração com outros módulos.
7. `shared` deve conter somente elementos realmente compartilhados.
8. `shared` não deve virar um local genérico para código sem classificação.
9. Dependências circulares devem ser evitadas.
10. Abstrações antecipadas devem ser evitadas quando não houver necessidade concreta.

## Persistência

A persistência utiliza:

- MySQL 8 como banco relacional.
- Spring Data JPA/Hibernate para persistência e validação do modelo.
- Flyway como mecanismo oficial e exclusivo de criação/evolução do schema.
- Testcontainers com MySQL para testes de integração quando apropriado.

Hibernate não deve criar ou alterar schema automaticamente. Os profiles devem usar:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Não utilizar:

- `ddl-auto=create`
- `ddl-auto=create-drop`
- `ddl-auto=update`

Migrations ficam em:

```text
backend/src/main/resources/db/migration
```

Convenção obrigatória:

```text
V<versão>__<descricao>.sql
```

Exemplos futuros:

```text
V1__initial_schema.sql
V2__create_users.sql
V3__create_accounts.sql
```

Migrations aplicadas em ambientes compartilhados não devem ser modificadas; correções devem ser feitas por novas migrations.

## API

A API será REST.

Regras:

- Controllers não devem conter regras de negócio.
- APIs REST devem utilizar DTOs específicos.
- Request e Response devem ser separados quando seus objetivos forem diferentes.
- Entidades JPA não devem ser expostas diretamente.
- Entidades JPA não devem ser utilizadas como payload público da API.
- APIs que retornarem coleções potencialmente grandes devem usar paginação server-side.
- Filtros e ordenações sobre grandes conjuntos devem ser executados preferencialmente no backend e seus parâmetros devem ser validados.

MapStruct poderá ser adicionado futuramente quando houver mapeamentos reais e redução clara de código repetitivo. Ele não deve ser introduzido por antecipação.

## Segurança

O Equilibra é multiusuário. O backend é responsável pela autorização e pelo isolamento dos dados dos usuários.

Regras obrigatórias:

- O frontend nunca será considerado barreira de segurança.
- Nenhum endpoint futuro deverá aceitar `userId` fornecido pelo frontend como fonte de autorização ou propriedade.
- A identidade do usuário deverá vir do contexto autenticado do Spring Security.
- Services e repositories deverão garantir isolamento por usuário quando apropriado.
- A autenticação efetiva será implementada em sprint própria.

Exemplos proibidos para determinar proprietário/autorização:

```text
POST /transactions?userId=123
```

```json
{
  "userId": "123"
}
```

## Categorias: domínio e persistência (TASK-2.4)

`category/domain/Category` representa uma classificação privada e plana, independente de contas de ativo. Possui UUID em `String`, `ownerId` imutável, `name`, `normalizedName`, `applicability`, `active`, timestamps `Instant` e versão otimista. Nesta etapa não há API, serviço de aplicação, seed ou interface de categorias.

- `CategoryApplicability`: `EXPENSE`, `INCOME` e `BOTH`, persistidos como texto.
- Novas categorias são ativas; `deactivate()` preserva o registro para histórico. `rename()`, `changeApplicability()` e `activate()` preservam o encapsulamento.
- Nome obrigatório com trim e limite de 100 caracteres. A apresentação preserva caixa e acentos; a normalização usa lowercase com `Locale.ROOT`, sem remover acentos, e também deve caber na coluna de 100 caracteres.
- `CategoryRepository` consulta por ID + owner, lista por owner, filtra ativas e filtra applicability via `IN`: despesas usam `EXPENSE/BOTH`; receitas usam `INCOME/BOTH`.
- V4 cria `categories` com FK para `users`, sem exclusão em cascata. O índice `(owner_id, active, applicability)` atende às consultas privadas; não há índice isolado redundante de owner.
- A coluna gerada `active_normalized_name` contém o nome normalizado somente quando ativa, ou `NULL` quando inativa. `UNIQUE(owner_id, active_normalized_name)` protege contra duplicidade ativa inclusive em operações concorrentes e independentemente de applicability, permitindo múltiplos registros históricos inativos de mesmo nome.
- Essa estratégia evita a limitação da constraint de V3 de contas de ativo, que também restringe duplicidade entre inativas. V3 e AssetAccount não são alterados nesta tarefa.
- A collation `utf8mb4_unicode_ci`, consistente com as tabelas existentes, compara sem distinguir caixa ou acentos: `Alimentação` e `Alimentacao` conflitam entre categorias ativas do mesmo owner. Isso não altera o texto armazenado/exibido. Lowercase Java não é a única regra de equivalência aplicada pelo banco.

A futura camada de aplicação deverá obter o owner exclusivamente de `CurrentUser.id()`. A entidade e o repository não dependem do contexto HTTP/Spring Security.

## Decisões financeiras

- Valores financeiros devem utilizar `BigDecimal`.
- Não utilizar `float`, `Float`, `double` ou `Double` para valores monetários.
- Precisão e escala deverão ser definidas explicitamente nas entidades financeiras futuras.
- Regras financeiras permanecem no backend.
- O frontend não deve implementar regras financeiras.
- Transferências financeiras futuras deverão ser atômicas.

## IDs

Para novas entidades de negócio, prefira UUID, salvo decisão arquitetural documentada diferente.

A representação externa dos IDs deverá ser consistente nas APIs.

Não criar entidades apenas para testar UUID.

## Datas, horários e timezone

Para eventos técnicos como `createdAt`, `updatedAt`, `loginAt` e timestamps de auditoria, prefira `Instant`.

Para data/hora informada pelo usuário em transações financeiras, a modelagem deverá considerar explicitamente timezone.

O sistema não deve tomar decisões implícitas baseadas no timezone do servidor. A estratégia de timezone deverá ser explicitada antes da implementação de transações financeiras.

## Transações de banco

`@Transactional` deve ser utilizado na camada de aplicação/service quando uma operação representar unidade atômica de negócio.

Evite `@Transactional` em controllers.

Transferências financeiras futuras deverão ser atômicas.

## Exceções

O backend deverá possuir estratégia centralizada de tratamento de erros.

Nesta etapa, essa estratégia é apenas documentada. A TASK-0.6 será responsável pelo tratamento global de erros e pelo contrato REST de erros.

Não criar mecanismos concorrentes de erro sem necessidade.

## Auditoria

O sistema deverá conseguir registrar futuramente eventos relevantes como:

- criação;
- alteração;
- exclusão;
- login;
- operações financeiras importantes.

O módulo de auditoria não deve ser implementado antes de task/sprint própria.

Não adicionar frameworks de auditoria sem necessidade concreta.

## Logging

Regras básicas:

- Utilizar SLF4J.
- Evitar `System.out.println`.
- Não registrar senhas.
- Não registrar tokens JWT.
- Não registrar conteúdo sensível de anexos.
- Evitar dados financeiros desnecessários nos logs.
- Utilizar níveis adequados de log.

## Configuração

Configurações específicas de ambiente devem permanecer externas ao código sempre que apropriado.

Segredos nunca devem ser versionados.

A estratégia atual utiliza:

- `application.yml`;
- profiles;
- variáveis de ambiente;
- `.env.example` para exemplos de desenvolvimento;
- `.env` real ignorado pelo Git.

## Multi-user data isolation

O Equilibra utiliza **isolamento lógico por usuário no mesmo banco/schema**, e não database-per-user ou schema-per-user.

### Princípios

1. A identidade confiável vem do `sub` do JWT, que contém o UUID do usuário, e é disponibilizada pelo Spring Security no `SecurityContext` através de `AuthenticatedPrincipal`.
2. Código de aplicação deve obter a identidade por `CurrentUser`, sem interpretar diretamente `SecurityContextHolder`, JWT ou claims.
3. O cliente nunca determina o proprietário via `userId` em query parameter, header ou request body. A propriedade é derivada de `CurrentUser.id()`.
4. Recursos privados devem possuir vínculo explícito com seu proprietário quando forem implementados.
5. Repositories privados devem usar consultas ownership-aware, como `findByIdAndOwnerId`, `findAllByOwnerId` e `existsByIdAndOwnerId`, evitando buscar por ID global e validar ownership apenas depois.
6. Criação, leitura, alteração e exclusão devem operar dentro do escopo do usuário autenticado.

Essa estratégia protege contra **IDOR/BOLA** (Insecure Direct Object Reference / Broken Object Level Authorization). Um usuário que conheça o UUID de recurso pertencente a outro usuário não poderá acessá-lo. Para recursos privados, a convenção preferencial é `404 Not Found` quando negar acesso também evita revelar a existência do recurso; `403 Forbidden` poderá ser usado quando a existência não for sensível.

Transferências futuras exigirão que origem e destino pertençam ao usuário autenticado. Anexos herdarão o ownership do recurso pai, relatórios consultarão apenas dados do `CurrentUser` e auditoria atribuirá ações à identidade autenticada, nunca a um `performedByUserId` enviado pelo cliente.

## Evolução

Decisões arquiteturais importantes futuras poderão ser documentadas por ADRs em:

```text
docs/adr/
```

O ADR inicial é:

```text
docs/adr/0001-modular-monolith.md
```

A arquitetura deve facilitar testes unitários e de integração.

Regras de negócio importantes devem poder ser testadas sem iniciar todo o servidor HTTP.

Integrações com banco devem usar Testcontainers quando apropriado.

Não utilizar H2 para substituir comportamentos específicos do MySQL.

## Multi-user data isolation

O Equilibra utiliza **isolamento lógico por usuário no mesmo banco/schema**, e não database-per-user ou schema-per-user.

### Princípios

1. A identidade confiável vem do `sub` do JWT, que contém o UUID do usuário, e é disponibilizada pelo Spring Security no `SecurityContext` através de `AuthenticatedPrincipal`.
2. Código de aplicação deve obter a identidade por `CurrentUser`, sem interpretar diretamente `SecurityContextHolder`, JWT ou claims.
3. O cliente nunca determina o proprietário via `userId` em query parameter, header ou request body. A propriedade é derivada de `CurrentUser.id()`.
4. Recursos privados devem possuir vínculo explícito com seu proprietário quando forem implementados.
5. Repositories privados devem usar consultas ownership-aware, como `findByIdAndOwnerId`, `findAllByOwnerId` e `existsByIdAndOwnerId`, evitando buscar por ID global e validar ownership apenas depois.
6. Criação, leitura, alteração e exclusão devem operar dentro do escopo do usuário autenticado.

Essa estratégia protege contra **IDOR/BOLA** (Insecure Direct Object Reference / Broken Object Level Authorization). Um usuário que conheça o UUID de recurso pertencente a outro usuário não poderá acessá-lo. Para recursos privados, a convenção preferencial é `404 Not Found` quando negar acesso também evita revelar a existência do recurso; `403 Forbidden` poderá ser usado quando a existência não for sensível.

Transferências futuras exigirão que origem e destino pertençam ao usuário autenticado. Anexos herdarão o ownership do recurso pai, relatórios consultarão apenas dados do `CurrentUser` e auditoria atribuirá ações à identidade autenticada, nunca a um `performedByUserId` enviado pelo cliente.

## Evolução

Decisões arquiteturais importantes futuras poderão ser documentadas por ADRs em:

```text
docs/adr/
```

O ADR inicial é:

```text
docs/adr/0001-modular-monolith.md
```
