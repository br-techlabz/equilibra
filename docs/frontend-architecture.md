# Arquitetura do Frontend - Equilibra

## Visão Geral

O frontend do Equilibra é uma aplicação Angular 19+ construída com as seguintes tecnologias:

- **Angular 19** — Framework principal
- **Angular Material** — Biblioteca de componentes UI
- **TypeScript** — Linguagem principal
- **SCSS** — Estilos
- **Standalone Components** — Arquitetura moderna sem NgModules
- **Angular Router** — Roteamento com lazy loading
- **Signals** — Estado reativo quando apropriado
- **Reactive Forms** — Formulários reativos
- **RxJS** — Programação reativa quando apropriado

---

## Estrutura de Diretórios

```
src/app/
├── core/                      # Infraestrutura global (HTTP, auth, etc.)
│   ├── http/                  # ProblemDetails, interceptors
│   └── guards/                # AuthGuard, GuestGuard
├── shared/                    # Componentes/pipes/diretivas/utilitários reutilizáveis
│   └── ui/                    # Componentes de UI compartilhados
│       ├── breadcrumb/        # Breadcrumb (navegação estrutural)
│       ├── content-panel/     # Painel de conteúdo reutilizável
│       ├── empty-state/       # Estado vazio padronizado
│       ├── page-header/       # Cabeçalho de página padrão
│       └── stat-card/         # Card de KPI financeiro
├── layout/                    # Estrutura visual principal
│   ├── shell/                 # AuthenticatedLayout (sidebar + header + outlet)
│   ├── header/                # Topbar (menu toggle, user menu, logout)
│   ├── sidebar/               # Sidebar escura (marca, menu hierárquico, user area)
│   └── public-layout/         # PublicLayout (login, register)
├── features/                  # Funcionalidades de negócio (lazy loaded)
│   ├── auth/
│   │   ├── data-access/       # AuthService, AuthApiService
│   │   └── pages/
│   │       ├── login/         # Login page (split layout desktop)
│   │       └── register/      # Register page (split layout desktop)
│   └── dashboard/
│       └── dashboard.page.*   # Dashboard com KPIs, chart panel, transactions panel
├── app.config.ts              # Providers globais
├── app.routes.ts              # Configuração de rotas (lazy loading)
└── app.component.ts           # Componente raiz
```

---

## Design System

O Equilibra possui um **Design System completo** documentado em [`docs/design-system.md`](design-system.md).

### Tokens Visuais (CSS Custom Properties)

Todos os valores visuais são centralizados em `src/styles.scss` como variáveis CSS:

- **Cores**: Paletas semânticas (primary, success, danger, warning, info, asset, grey)
- **Cores de UI**: `--card-bg`, `--card-border`, `--sidebar-bg`, `--topbar-bg`, `--text-primary`, etc.
- **Tipografia**: `--font-family-base`, escala `--font-size-*`, pesos, line-heights
- **Espaçamento**: Base 4px (`--space-1` a `--space-16`)
- **Raio**: `--radius-sm` a `--radius-2xl`, `--radius-full`
- **Sombras**: `--shadow-sm` a `--shadow-2xl`
- **Transições**: `--transition-fast/normal/slow`
- **Breakpoints**: `--bp-mobile` (768px), `--bp-tablet` (1024px), `--bp-desktop` (1280px), `--bp-wide` (1920px)

**Regra obrigatória:** Nunca use valores hexadecimais, pixels ou rem hardcoded nos componentes. Use sempre os tokens.

### Componentes Compartilhados (Shared UI)

| Componente | Seletor | Descrição |
|------------|---------|-----------|
| `PageHeaderComponent` | `app-page-header` | Título + subtítulo + ações |
| `StatCardComponent` | `app-stat-card` | KPI com variante (income, expense, balance, asset, neutral) |
| `ContentPanelComponent` | `app-content-panel` | Painel com header, conteúdo (projection), footer |
| `EmptyStateComponent` | `app-empty-state` | Estado vazio com ícone variante, título, descrição, ações |
| `BreadcrumbComponent` | `app-breadcrumb` | Navegação estrutural auto-gerada da rota |

---

## Layouts

### AuthenticatedLayout (`app-shell`)

Layout para páginas autenticadas. Composto por:

```
┌─────────────────────────────────────────────────────────────┐
│ TOPBAR (app-header) — 56px                                  │
├──────────────────┬──────────────────────────────────────────┤
│                  │                                          │
│   SIDEBAR        │         MAIN CONTENT                     │
│   (app-sidebar)  │         (router-outlet)                  │
│   256px / 72px   │                                          │
│                  │                                          │
└──────────────────┴──────────────────────────────────────────┘
```

- **Desktop (≥ 1024px):** Sidebar persistente (`mode="side"`), topbar fixa
- **Mobile (< 768px):** Sidebar vira drawer overlay (`mode="over"`), abre via botão da topbar, fecha ao navegar
- **Tablet (768–1023px):** Sidebar recolhível, comporta-se como desktop

### PublicLayout (`app-public-layout`)

Layout para páginas públicas (login, register):
- Centraliza conteúdo vertical/horizontalmente
- Fundo cinza claro (`--color-canvas`)
- Sem sidebar/topbar
- Usado por `/login` e `/register`

---

## Componentes Principais de Layout

### ShellComponent (`app-shell`)
- Orquestra `mat-sidenav-container` com sidebar, header e `<router-outlet>`
- Gerencia estado mobile/desktop via `@HostListener('window:resize')` (breakpoint 768px)
- Conecta outputs da sidebar/header (logout, toggle, navigation)
- Expõe `currentUserEmail` e `isAuthenticated` via `AuthService` (computed signals)

### HeaderComponent (`app-header`)
- **Seletor:** `app-header`
- Topbar escura (`--topbar-bg`)
- Toggle sidebar (hamburger no mobile, chevron no desktop recolhido)
- Slot `[breadcrumb]` para projeção do breadcrumb
- Área do usuário: avatar + email + menu dropdown (logout)
- Inputs: `userEmail`, `sidebarCollapsed`
- Outputs: `menuToggle`, `sidebarCollapseToggle`, `logout`

### SidebarComponent (`app-sidebar`)
- **Seletor:** `app-sidebar`
- Fundo escuro (`--sidebar-bg`), largura 256px (expandida) / 72px (recolhida)
- **Marca:** Ícone + "Equilibra" + tagline "Finanças Domésticas"
- **Menu hierárquico** com `MatExpansionPanel`:
  - Dashboard
  - Contas → Contas de ativos, Contas de despesas
  - Categorias
  - Transações → Despesas, Receitas, Transferências
  - Relatórios
- Item ativo: barra lateral accent + background `--sidebar-active`
- Tooltip automático em modo recolhido
- **Área do usuário (bottom):** Email + "Minha conta" + menu com logout
- Inputs: `collapsed`, `isMobile`
- Outputs: `navigationClick`, `logout`
- Navegação por teclado acessível

### BreadcrumbComponent (`app-breadcrumb`)
- **Seletor:** `app-breadcrumb`
- Gerado automaticamente a partir do `Router` + `NavigationEnd`
- Mapeia segmentos de URL para labels legíveis (ex: `Home / Transações / Despesas`)
- Oculto em mobile (< 768px)
- Home link com ícone, separadores `chevron_right`

---

## Páginas de Autenticação

### LoginPageComponent (`/login`)
- **Layout desktop:** Split — branding à esquerda (gradiente primary), formulário à direita (card)
- **Mobile/Tablet:** Card centralizado full-width
- **Formulário:** Reactive Forms, email + senha, mostrar/ocultar senha
- **Validação:** Required, email, min/max length
- **Erros:** Inline (mat-error) + server errors via ProblemDetails
- **Loading:** Spinner no botão, disabled durante submit
- **Link para register**

### RegisterPageComponent (`/register`)
- Mesmo layout split do login
- **Branding:** Ícone, título, tagline, 3 feature items com check icons
- **Formulário:** Email, senha, confirmar senha (validator custom `passwordsMatchValidator`)
- **Validação:** Required, email, min 8 / max 128 chars, not only whitespace, match
- **Erros:** Inline + server (400, 409, 0)
- **Link para login**

---

## Dashboard

### DashboardPageComponent (`/dashboard`)
O Dashboard combina os KPIs neutros existentes com dados reais de `AssetAccount` disponíveis nesta etapa:

1. **PageHeader** — "Dashboard" + "Visão geral das suas finanças".
2. **KPI Grid**: Entradas, Saídas e Saldo permanecem `R$ 0,00`, pois dependem de transações; Patrimônio usa a soma dos `initialBalance` das contas ativas.
3. **Painéis principais**: evolução do saldo e transações recentes continuam em empty state, sem séries ou lançamentos fictícios.
4. **Minhas contas**: lista contas ativas reais, exibindo nome, tipo traduzido e `Saldo inicial`, com link para `/accounts`. Sem contas ativas, exibe empty state e ação de cadastro.

A chamada reutiliza `AssetAccountsApiService.list(false)`. O patrimônio é calculado em centavos inteiros antes da formatação BRL, evitando erros binários como `0,10 + 0,20`; valores negativos são preservados. Loading e erro do endpoint têm estados distintos: erro não é apresentado como patrimônio zero e permite retry.

**Limitação explícita / dívida técnica:** nesta etapa, patrimônio significa “baseado nos saldos iniciais das contas ativas”. Quando o ledger e transações existirem, substituir por `currentBalance` derivado de `initialBalance + receitas - despesas + transferências recebidas - transferências enviadas`, sem usar `createdAt` para fabricar histórico.

**Responsividade:**
- KPIs: 1 coluna (mobile) → 2 (tablet) → 4 (desktop).
- Painéis e resumo de contas: empilhados em telas menores, sem overflow horizontal.

---

## Roteamento

```typescript
// app.routes.ts — Lazy loading para features
{
  path: 'auth',
  loadChildren: () => import('./features/auth/auth.routes'),
},
{
  path: 'dashboard',
  loadComponent: () => import('./features/dashboard/dashboard.page')
    .then(m => m.DashboardPageComponent),
  canActivate: [authGuard],
},
{
  path: '',
  redirectTo: '/dashboard',
  pathMatch: 'full',
},
```

- **Auth routes** (`/login`, `/register`): `PublicLayout`, `guestGuard`
- **Protected routes** (`/dashboard`, futuras): `AuthenticatedLayout` (via `app-shell`), `authGuard`
- **Root redirect:** `/` → `/dashboard`

---

## Serviços de Autenticação

### AuthService
- **Signal-based state:** `currentUser` (User | null), `isAuthenticated` (computed)
- **Token:** Armazenado **apenas em memória** (variável privada), NUNCA em localStorage/sessionStorage
- **Métodos:** `login()`, `register()`, `logout()`, `clearSession()`, `getToken()`, `setSession()`
- **Logout:** Limpa token + usuário + navega para `/login`

### AuthApiService
- HTTP calls para `/api/auth/login`, `/api/auth/register`, `/api/users/me`
- Retorna `ProblemDetails` tipado para erros
- Usa `HttpClient` com interceptors (JWT, Request-ID)

---

## Guards

### AuthGuard (`canActivate`)
- Verifica `authService.isAuthenticated()`
- Se false: `clearSession()` + redirect `/login`
- Usado em rotas protegidas

### GuestGuard (`canActivate`)
- Verifica `!authService.isAuthenticated()`
- Se true: redirect `/dashboard`
- Usado em `/login`, `/register`

---

## Interceptors HTTP

### JwtInterceptor
- Adiciona `Authorization: Bearer <token>` se `AuthService.getToken()` existir
- Skip para URLs de auth públicas (`/api/auth/**`)

### RequestIdInterceptor
- Adiciona `X-Request-ID` (UUID v4) a toda requisição
- Propaga `X-Request-ID` do response para correlação

---

## Configuração de Ambiente

```
src/environments/
├── environment.ts         # Desenvolvimento (http://localhost:8080/api)
└── environment.prod.ts    # Produção (https://api.equilibra.com.br/api)
```

- Configuração via `fileReplacements` no `angular.json`
- URL da API centralizada em `environment.apiUrl`

---

## Estado

- **Signals** — Estado local/síncrono (formulários, UI state, currentUser)
- **RxJS** — Fluxos assíncronos/HTTP (login, register, logout)
- **Services** — Lógica compartilhada (AuthService, AuthApiService)
- **NgRx/Akita** — NÃO instalado (só quando complexidade justificar)

---

## Formulários

- **Reactive Forms** como padrão (`NonNullableFormBuilder`)
- `Validators` do Angular + validators customizados
- Integração com `ProblemDetails` do backend para erros de servidor
- Acessibilidade: labels, `aria-describedby` nos erros, `autocomplete`

---

## Testes

- **Karma + Jasmine** para testes unitários
- **TestBed** com `provideRouter([])` para componentes standalone
- Testes existentes: `AppComponent`, `AuthService`, `AuthApiService`, `RegisterPageComponent`
- Comando: `npm test -- --watch=false --browsers=ChromeHeadless`

---

## Qualidade de Código

- **ESLint** com `@angular-eslint` (strict)
- **Prettier** para formatação
- **TypeScript strict** mode
- Lint: `npm run lint` ✅
- Format: `npm run format`
- Build: `npm run build` ✅

---

## Build

- `npm run build` — Build de produção (output: `dist/frontend/`)
- **Budgets:** Initial 600kB (warning), 1MB (error); Component styles 4kB (warning)
- **Lazy loading chunks:** `auth-routes`, `login-page`, `register-page`, `dashboard-page`
- **Atual status:** Build passing, 21 testes passing, lint clean

---

## Próximos Passos (Próximas Sprints)

1. **Contas** — CRUD de contas de ativos/despesas, saldos
2. **Categorias** — Hierarquia de categorias receita/despesa
3. **Transações** — Lançamentos, listagem, filtros, paginação
4. **Transferências** — Entre contas, atômicas
5. **Relatórios** — Gráficos reais (bibliotecas de chart), exportação
6. **Anexos** — Upload de comprovantes
7. **Dashboard funcional** — Dados reais via API, gráficos interativos
8. **Testes E2E** — Playwright (desktop + mobile flows)
9. **Dark mode** — Toggle de tema
10. **Internacionalização** — i18n (pt-BR base, preparado para outros)