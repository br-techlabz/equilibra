# Equilibra

Aplicação web multiusuário para gerenciamento de finanças domésticas.

---

## Arquitetura Geral

O Equilibra segue uma arquitetura **monólito modular** no backend, organizada por domínios de negócio, com frontend desacoplado em Angular.

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (Angular 19)                    │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐            │
│  │  Auth   │  │Dashboard│  │ Accounts│  │ Reports │   ...      │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘            │
└─────────────────────────────┬───────────────────────────────────┘
                              │ REST API (JSON)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      BACKEND (Spring Boot 4)                    │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐        │
│  │  auth  │ │ user   │ │account │ │category│ │transaction│      │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘        │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐        │
│  │transfer│ │attach. │ │dashboard│ │ report │ │ audit  │        │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                      shared (common)                      │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────┘
                              │ JDBC
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DATABASE (MySQL 8)                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## Stack Prevista

### Backend
- **Java 21** (LTS)
- **Spring Boot 4**
- **Maven** (build)
- **Spring Web** (REST)
- **Spring Security** (auth/authorization)
- **Spring Data JPA** + **Hibernate** (ORM)
- **Jakarta Bean Validation** (validação)
- **MySQL 8** (banco de dados)
- **Flyway** (migrations)
- **OpenAPI** (documentação da API)
- **JUnit 5**, **Mockito**, **Testcontainers** (testes)

### Frontend
- **Angular 19**
- **TypeScript**
- **Angular Material** (UI components)
- **SCSS** (estilos)
- **Standalone Components**
- **Signals** (reactividade)
- **Reactive Forms**
- **Lazy Loading** (rotas)
- **Playwright** (testes E2E)

### Infraestrutura
- **Docker** + **Docker Compose** (containerização)
- **GitHub Actions** (CI/CD)

---

## Estrutura do Repositório

```
equilibra/
├── backend/          # Spring Boot application
├── frontend/         # Angular application
├── docs/             # Documentação do projeto
├── .github/          # GitHub Actions workflows
├── .gitignore
├── CLAUDE.md         # Instruções para desenvolvimento assistido
├── docker-compose.yml
└── README.md
```

> **Nota:** Os diretórios `backend/` e `frontend/` serão inicializados nas próximas tasks (TASK-0.2 e TASK-0.3).

---

## Pré-requisitos de Desenvolvimento

| Ferramenta | Versão Mínima | Observação |
|------------|---------------|------------|
| Java | 21 (LTS) | Para Spring Boot 4 |
| Maven | 3.9+ | Build do backend |
| Node.js | 20+ (LTS) | Para Angular 19 |
| npm | 10+ | Gerenciador de pacotes |
| Angular CLI | 19+ | `npm install -g @angular/cli` |
| Docker | 24+ | Containerização |
| Docker Compose | 2.20+ | Orquestração local |
| MySQL | 8.0 | Banco de dados (via Docker) |

---

## Instruções de Execução

### Infraestrutura (Docker Compose)

#### Iniciar todos os serviços
```bash
# Copiar arquivo de exemplo de variáveis de ambiente
cp .env.example .env

# Subir infraestrutura (MySQL + Backend)
docker compose up -d
```

#### Parar infraestrutura
```bash
# Parar containers (mantém volumes)
docker compose down

# Parar containers e remover volumes (CUIDADO: apaga dados do banco)
docker compose down -v
```

#### Visualizar logs
```bash
# Logs de todos os serviços
docker compose logs -f

# Logs apenas do MySQL
docker compose logs -f mysql

# Logs apenas do Backend
docker compose logs -f backend
```

#### Verificar status
```bash
# Status dos containers
docker compose ps

# Health checks
curl http://localhost:8080/api/actuator/health
```

### Backend (Desenvolvimento Local)

#### Pré-requisitos
- Java 21
- Maven 3.9+ (ou usar wrapper `./mvnw`)

#### Build e testes
```bash
cd backend

# Compilar
./mvnw clean compile

# Executar testes
./mvnw test

# Build completo (compila + testes + package)
./mvnw clean package
```

#### Executar localmente (requer MySQL rodando)
```bash
cd backend

# Profile dev (MySQL local na porta 3306)
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Profile docker (conecta no MySQL do docker-compose)
./mvnw spring-boot:run -Dspring.profiles.active=docker
```

#### Endpoints disponíveis
- **Health Check**: http://localhost:8080/api/actuator/health
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/api/api-docs

### Migrations de Banco de Dados

O Equilibra utiliza **Flyway** como mecanismo oficial e exclusivo para criação e evolução do schema do banco.

- Scripts ficam em `backend/src/main/resources/db/migration`.
- O padrão obrigatório é `V<versão>__<descricao>.sql`.
- Exemplos futuros: `V1__initial_schema.sql`, `V2__create_users.sql`.
- O Flyway executa automaticamente as migrations no startup do backend, usando o mesmo datasource da aplicação.
- Hibernate/JPA valida o schema com `ddl-auto: validate`; não deve criar ou alterar tabelas automaticamente.
- Migrations já aplicadas em ambientes compartilhados não devem ser modificadas; correções devem ser feitas por novas migrations.

### Frontend (pendente)
```bash
# TODO: comandos de build e execução
```

---

## Documentação Adicional

- [CLAUDE.md](CLAUDE.md) — Regras e fluxo de desenvolvimento assistido
- [docs/architecture.md](docs/architecture.md) — Arquitetura base do backend
- [docs/adr/0001-modular-monolith.md](docs/adr/0001-modular-monolith.md) — ADR inicial: monólito modular
- [docs/](docs/) — Documentação técnica e de arquitetura