# Arquitetura do Frontend - Equilibra

## Visão Geral

O frontend do Equilibra é uma aplicação Angular 19+ construída com as seguintes tecnologias:

- **Angular 19** - Framework principal
- **Angular Material** - Biblioteca de componentes UI
- **TypeScript** - Linguagem principal
- **SCSS** - Estilos
- **Standalone Components** - Arquitetura moderna sem NgModules
- **Angular Router** - Roteamento com lazy loading
- **Signals** - Estado reativo quando apropriado
- **Reactive Forms** - Formulários reativos para formulários futuros
- **RxJS** - Programação reativa quando apropriado

## Estrutura de Diretórios

```
src/app/
├── core/                    # Infraestrutura global (futuro)
├── shared/                  # Componentes/pipes/diretivas/utilitários reutilizáveis
├── layout/                  # Estrutura visual principal
│   ├── shell/               # Application shell principal
│   ├── header/              # Header/Toolbar
│   └── sidebar/             # Sidebar/Drawer de navegação
├── features/                # Funcionalidades de negócio
│   └── dashboard/           # Dashboard (placeholder)
└── app.routes.ts            # Configuração de rotas
```

## Componentes Principais

### ShellComponent
O componente raiz que orquestra o layout da aplicação:
- Toolbar/Header fixo no topo
- Sidebar/Drawer responsiva (side no desktop, drawer no mobile)
- Área principal com `<router-outlet>`
- Gerencia estado mobile/desktop via `@HostListener('window:resize')`

### HeaderComponent
- Logo/identidade "Equilibra"
- Botão de menu hamburger para mobile
- Área reservada para usuário autenticado (placeholder)

### SidebarComponent
- Navegação principal com itens expansíveis
- Grupos: Dashboard, Cadastros, Transações, Relatórios
- Navegação por teclado e acessível
- Indicador visual de item ativo

### DashboardPageComponent
Página inicial temporária com:
- 4 cards de resumo (Saldo Total, Receitas, Despesas, Saldo Projetado)
- Estado vazio informativo
- Layout responsivo (1/2/4 colunas conforme breakpoint)

## Identidade Visual

### Cores
- **Primária (Teal)**: #009688 (confiança, equilíbrio)
- **Accent (Green)**: #4caf50 (positivo, finanças)
- **Warn (Red)**: #f44336 (negativo, alertas)
- **Neutros**: Escala de cinza para superfícies e textos

### Tipografia
- Fonte base: Roboto (sistema)
- Hierarquia: 3xl (títulos), xl (subtítulos), base (corpo), sm (labels)

### Espaçamento
- Sistema baseado em 0.25rem (xs, sm, md, lg, xl, 2xl)

### Responsividade
- **Mobile**: < 600px (drawer, 1 coluna)
- **Tablet**: 600-959px (drawer, 2 colunas)
- **Desktop**: ≥ 960px (sidebar fixa, 4 colunas)

## Estratégia de Roteamento

```typescript
// Lazy loading para features
{
  path: 'dashboard',
  loadComponent: () => import('./features/dashboard/dashboard.page')
    .then(m => m.DashboardPageComponent),
}
```

- Rota raiz redireciona para `/dashboard`
- Lazy loading preparado para features futuras
- Shell contém o `<router-outlet>` principal

## Configuração de Ambiente

```
src/environments/
├── environment.ts         # Desenvolvimento (localhost:8080/api)
└── environment.prod.ts    # Produção (api.equilibra.com.br)
```

- Configuração via `fileReplacements` no angular.json
- URL da API centralizada em `environment.apiUrl`

## HTTP Client

Preparado para uso futuro com:
- `provideHttpClient(withInterceptorsFromDi())` no `app.config.ts`
- Interceptors JWT futuros
- Request ID propagation (TASK-0.8)

## Estado

- **Signals** para estado local/síncrono
- **RxJS** para fluxos assíncronos/HTTP
- **Services** para lógica compartilhada
- **NgRx/Akita** - NÃO instalado (só quando complexidade justificar)

## Formulários

- **Reactive Forms** como padrão
- `Validators` do Angular para validação
- Integração futura com `ProblemDetail` do backend

## Testes

- **Karma + Jasmine** para testes unitários
- **TestBed** com `provideRouter([])` para componentes standalone
- Testes de componente: shell, header, sidebar, dashboard

## Qualidade de Código

- **ESLint** com `@angular-eslint` (strict)
- **Prettier** para formatação
- **TypeScript strict** mode
- Lint: `npm run lint`
- Format: `npm run format`

## Build

- `npm run build` - Build de produção (output: `dist/frontend/`)
- Budgets: 600kB warning, 1MB error
- Lazy loading para dashboard (chunk separado)

## Próximos Passos (TASK-0.8)

1. Integração HTTP real com backend
2. Autenticação JWT
3. Interceptors HTTP
3. Páginas de features reais (Contas, Categorias, Transações)
4. Testes E2E com Playwright
5. Dark mode
6. Internacionalização (i18n)