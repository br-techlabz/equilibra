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

> ⚠️ **EM CONSTRUÇÃO** — Esta seção será completada conforme as tasks avançarem.

### Backend (pendente TASK-0.2)
```bash
# TODO: comandos de build e execução
```

### Frontend (pendente)
```bash
# TODO: comandos de build e execução
```

### Docker Compose (pendente TASK-0.3)
```bash
# TODO: docker-compose up -d
```

---

## Documentação Adicional

- [CLAUDE.md](CLAUDE.md) — Regras e fluxo de desenvolvimento
- [docs/](docs/) — Documentação técnica e de arquitetura