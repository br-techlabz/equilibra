# Design System — Equilibra

> Sistema de design visual para a aplicação **Equilibra — Gestão de Finanças Domésticas**.

---

## Identity

**Equilibra** — Aplicação web multiusuário para gerenciamento de finanças domésticas.

O design system traduz a identidade visual da aplicação: moderna, limpa, profissional e acessível. Inspirado em dashboards administrativos contemporâneos, porém com identidade própria voltada ao contexto financeiro doméstico.

---

## Colors

### Tokens Semânticos (CSS Custom Properties)

Todas as cores são definidas como variáveis CSS em `src/styles.scss`. Nunca utilize valores hexadecimais diretamente nos componentes.

#### Paleta Primária (Azul Financeiro)
```scss
--color-primary-50:  #eff6ff;
--color-primary-100: #dbeafe;
--color-primary-200: #bfdbfe;
--color-primary-300: #93c5fd;
--color-primary-400: #60a5fa;
--color-primary-500: #3b82f6;  // Cor principal
--color-primary-600: #2563eb;
--color-primary-700: #1d4ed8;
--color-primary-800: #1e40af;
--color-primary-900: #1e3a8a;
--color-primary-950: #172554;
```

#### Paleta de Sucesso (Verde Financeiro — Entradas/Receitas)
```scss
--color-success-50:  #f0fdf4;
--color-success-100: #dcfce7;
--color-success-200: #bbf7d0;
--color-success-300: #86efac;
--color-success-400: #4ade80;
--color-success-500: #22c55e;
--color-success-600: #16a34a;
--color-success-700: #15803d;
--color-success-800: #166534;
--color-success-900: #14532d;
```

#### Paleta de Perigo (Coral/Vermelho Suave — Saídas/Despesas)
```scss
--color-danger-50:  #fef2f2;
--color-danger-100: #fee2e2;
--color-danger-200: #fecaca;
--color-danger-300: #fca5a5;
--color-danger-400: #f87171;
--color-danger-500: #ef4444;
--color-danger-600: #dc2626;
--color-danger-700: #b91c1c;
--color-danger-800: #991b1b;
--color-danger-900: #7f1d1d;
```

#### Paleta de Aviso (Âmbar)
```scss
--color-warning-50:  #fffbeb;
--color-warning-100: #fef3c7;
--color-warning-200: #fde68a;
--color-warning-300: #fcd34d;
--color-warning-400: #fbbf24;
--color-warning-500: #f59e0b;
--color-warning-600: #d97706;
--color-warning-700: #b45309;
--color-warning-800: #92400e;
--color-warning-900: #78350f;
```

#### Paleta de Informação (Azul Claro)
```scss
--color-info-50:  #eff6ff;
--color-info-100: #dbeafe;
--color-info-200: #bfdbfe;
--color-info-300: #93c5fd;
--color-info-400: #60a5fa;
--color-info-500: #3b82f6;
--color-info-600: #2563eb;
--color-info-700: #1d4ed8;
--color-info-800: #1e40af;
--color-info-900: #1e3a8a;
```

#### Paleta de Ativo/Patrimônio (Teal/Petróleo)
```scss
--color-asset-50:  #f0fdfa;
--color-asset-100: #ccfbf1;
--color-asset-200: #99f6e4;
--color-asset-300: #5eead4;
--color-asset-400: #2dd4bf;
--color-asset-500: #14b8a6;  // Cor principal de ativo
--color-asset-600: #0d9488;
--color-asset-700: #0f766e;
--color-asset-800: #115e59;
--color-asset-900: #134e4a;
```

#### Escala Neutra (Cinza)
```scss
--color-grey-50:  #f8fafc;
--color-grey-100: #f1f5f9;
--color-grey-200: #e2e8f0;
--color-grey-300: #cbd5e1;
--color-grey-400: #94a3b8;
--color-grey-500: #64748b;
--color-grey-600: #475569;
--color-grey-700: #334155;
--color-grey-800: #1e293b;
--color-grey-900: #0f172a;
--color-grey-950: #020617;
```

### Cores Semânticas de UI

```scss
// Canvas (fundo principal)
--color-canvas: var(--color-grey-50);
--color-canvas-alt: var(--color-grey-100);

// Cards/Paineis
--card-bg: #ffffff;
--card-border: var(--color-grey-200);
--card-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.05), 0 1px 2px -1px rgb(0 0 0 / 0.05);

// Sidebar
--sidebar-bg: var(--color-grey-950);
--sidebar-text: #ffffff;
--sidebar-text-muted: var(--color-grey-400);
--sidebar-hover: var(--color-grey-900);
--sidebar-active: var(--color-primary-900);
--sidebar-active-text: #ffffff;
--sidebar-divider: var(--color-grey-800);

// Topbar
--topbar-bg: var(--color-grey-900);
--topbar-text: #ffffff;
--topbar-text-muted: var(--color-grey-400);

// Texto
--text-primary: var(--color-grey-900);
--text-secondary: var(--color-grey-600);
--text-tertiary: var(--color-grey-500);
--text-on-primary: #ffffff;
--text-on-danger: #ffffff;

// Estados de formulário
--input-bg: #ffffff;
--input-border: var(--color-grey-300);
--input-border-focus: var(--color-primary-500);
--input-border-error: var(--color-danger-500);
--input-placeholder: var(--color-grey-400);
```

---

## Typography

### Fonte
Utiliza a fonte do sistema (`system-ui`) para performance e consistência nativa. Não requer fontes externas.

```scss
--font-family-base: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
--font-family-mono: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, 'Liberation Mono', monospace;
```

### Escala Tipográfica
```scss
--font-size-xs:     0.75rem;   // 12px
--font-size-sm:     0.875rem;  // 14px
--font-size-body-sm: 0.875rem; // 14px (alias semântico)
--font-size-body:   1rem;      // 16px
--font-size-lg:     1.125rem;  // 18px
--font-size-xl:     1.25rem;   // 20px
--font-size-2xl:    1.5rem;    // 24px
--font-size-3xl:    1.875rem;  // 30px
--font-size-4xl:    2.25rem;   // 36px
--font-size-h1:     var(--font-size-4xl);
--font-size-h2:     var(--font-size-3xl);
--font-size-h3:     var(--font-size-2xl);
--font-size-caption: var(--font-size-xs);
```

### Pesos
```scss
--font-normal: 400;
--font-medium: 500;
--font-semibold: 600;
--font-bold: 700;
```

### Alturas de Linha
```scss
--line-height-tight: 1.25;
--line-height-normal: 1.5;
--line-height-relaxed: 1.625;
```

---

## Spacing

Base: 4px (0.25rem). Escala consistente:

```scss
--space-0: 0;
--space-1: 0.25rem;  // 4px
--space-2: 0.5rem;   // 8px
--space-3: 0.75rem;  // 12px
--space-4: 1rem;     // 16px
--space-5: 1.25rem;  // 20px
--space-6: 1.5rem;   // 24px
--space-7: 1.75rem;  // 28px
--space-8: 2rem;     // 32px
--space-9: 2.25rem;  // 36px
--space-10: 2.5rem;  // 40px
--space-12: 3rem;    // 48px
--space-16: 4rem;    // 64px
```

---

## Radius

```scss
--radius-none: 0;
--radius-sm: 0.25rem;   // 4px
--radius-md: 0.375rem;  // 6px
--radius-lg: 0.5rem;    // 8px
--radius-xl: 0.75rem;   // 12px
--radius-2xl: 1rem;     // 16px
--radius-full: 9999px;
```

---

## Shadows

```scss
--shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
--shadow-base: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
--shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
--shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
--shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
--shadow-2xl: 0 25px 50px -12px rgb(0 0 0 / 0.25);
```

---

## Layout

### Breakpoints
```scss
--bp-mobile: 768px;
--bp-tablet: 1024px;
--bp-desktop: 1280px;
--bp-wide: 1920px;
```

### Estrutura Principal (Desktop ≥ 1024px)

```
┌─────────────────────────────────────────────────────────────┐
│ TOPBAR (56px)                                               │
├──────────────────┬──────────────────────────────────────────┤
│                  │                                          │
│   SIDEBAR        │         MAIN CONTENT                     │
│   (256px)        │         (flex: 1)                        │
│                  │                                          │
│                  │  ┌──────────────────────────────────┐   │
│                  │  │ Page Header (title + actions)    │   │
│                  │  ├──────────────────────────────────┤   │
│                  │  │ KPI Grid (4 cards)               │   │
│                  │  ├──────────────────┬───────────────┤   │
│                  │  │ Chart Panel      │ Transactions  │   │
│                  │  │ (65-70%)         │ Panel (30-35%)│   │
│                  │  └──────────────────┴───────────────┘   │
└──────────────────┴──────────────────────────────────────────┘
```

### Dimensões
- **Sidebar**: 256px (expandida) / 72px (recolhida)
- **Topbar**: 56px
- **Content padding**: 24px (desktop), 16-20px (tablet), 12-16px (mobile)

### Mobile (< 768px)
- Sidebar vira drawer (overlay) acionado pelo botão da topbar
- Topbar mantém 56px
- KPI cards empilham em 1 coluna
- Chart e Transactions empilham verticalmente

---

## Components

### 1. PageHeader (`app-page-header`)
**Seletor:** `app-page-header`  
**Localização:** `src/app/shared/ui/page-header/`

Cabeçalho padrão de página com título, subtítulo opcional e ações à direita.

**Inputs:**
- `title` (required): string
- `subtitle`: string
- `actions`: `PageHeaderAction[]`

**Interface `PageHeaderAction`:**
```typescript
{
  label: string;
  icon: string;
  handler: () => void;
  tooltip?: string;
  color?: 'primary' | 'accent' | 'warn';
  variant?: 'flat' | 'stroked' | 'icon';
}
```

---

### 2. StatCard (`app-stat-card`)
**Seletor:** `app-stat-card`  
**Localização:** `src/app/shared/ui/stat-card/`

Card de KPI financeiro com ícone, valor, label, tendência opcional e ação opcional.

**Inputs:**
- `label` (required): string
- `value` (required): string
- `icon` (required): string (Material Icon name)
- `variant`: `'income' | 'expense' | 'balance' | 'asset' | 'neutral'` (default: `'neutral'`)
- `trend`: string
- `trendPositive`: boolean (default: true)
- `action`: `StatCardAction | null`

**Variantes de cor:**
- `income` — Verde (entradas/receitas)
- `expense` — Coral (saídas/despesas)
- `balance` — Azul (saldo)
- `asset` — Teal (patrimônio)
- `neutral` — Cinza (genérico)

**Interface `StatCardAction`:**
```typescript
{
  label: string;
  icon: string;
  handler: () => void;
  tooltip?: string;
  disabled?: boolean;
}
```

---

### 3. ContentPanel (`app-content-panel`)
**Seletor:** `app-content-panel`  
**Localização:** `src/app/shared/ui/content-panel/`

Painel de conteúdo reutilizável com header (título, subtítulo, ações), conteúdo (projection) e footer opcional.

**Inputs:**
- `title`: string
- `subtitle`: string
- `actions`: `PanelAction[]`

**Content projection:** `<ng-content>` para conteúdo principal  
**Footer:** `<ng-template>` via `contentChild`

**Interface `PanelAction`:**
```typescript
{
  label: string;
  icon: string;
  handler: () => void;
  tooltip?: string;
  color?: 'primary' | 'accent' | 'warn';
  variant?: 'flat' | 'stroked' | 'icon';
  disabled?: boolean;
}
```

---

### 4. EmptyState (`app-empty-state`)
**Seletor:** `app-empty-state`  
**Localização:** `src/app/shared/ui/empty-state/`

Estado vazio padronizado com ícone, título, descrição e ações opcionais.

**Inputs:**
- `icon` (required): string (Material Icon name)
- `iconVariant`: `'income' | 'expense' | 'balance' | 'asset' | 'neutral' | 'search' | 'inbox' | 'chart' | 'folder'` (default: `'neutral'`)
- `title` (required): string
- `description`: string
- `centered`: boolean (default: true)
- `actions`: `EmptyStateAction[]`

**Interface `EmptyStateAction`:**
```typescript
{
  label: string;
  icon?: string;
  handler: () => void;
  color?: 'primary' | 'accent' | 'warn';
  variant?: 'flat' | 'stroked' | 'icon';
  tooltip?: string;
  disabled?: boolean;
}
```

---

### 5. Breadcrumb (`app-breadcrumb`)
**Seletor:** `app-breadcrumb`  
**Localização:** `src/app/shared/ui/breadcrumb/`

Navegação estrutural gerada automaticamente a partir da rota atual.

**Funcionamento:**
- Baseia-se no `Router` e eventos `NavigationEnd`
- Mapeia segmentos de URL para labels legíveis
- Oculto em mobile (< 768px)
- Exemplo: `Home / Transações / Despesas`

---

### 6. Sidebar (`app-sidebar`)
**Seletor:** `app-sidebar`  
**Localização:** `src/app/layout/sidebar/`

Sidebar escura com marca, menu hierárquico e área do usuário.

**Features:**
- Largura: 256px (expandida) / 72px (recolhida)
- Menu hierárquico com expansão/recolhimento (MatExpansionPanel)
- Item ativo destacado com barra lateral accent + background
- Tooltip em modo recolhido
- Área do usuário com email e menu (logout)
- Responsiva: drawer overlay em mobile

**Inputs:**
- `collapsed`: boolean
- `isMobile`: boolean

**Outputs:**
- `navigationClick`: EventEmitter<void>
- `logout`: EventEmitter<void>

---

### 7. Header / Topbar (`app-header`)
**Seletor:** `app-header`  
**Localização:** `src/app/layout/header/`

Topbar escura com toggle sidebar, breadcrumbs (slot), avatar/email do usuário e menu dropdown com logout.

**Inputs:**
- `userEmail`: string
- `sidebarCollapsed`: boolean

**Outputs:**
- `menuToggle`: EventEmitter<void>
- `sidebarCollapseToggle`: EventEmitter<void>
- `logout`: EventEmitter<void>

**Content projection:** `[breadcrumb]` slot para breadcrumbs

---

### 8. Shell / AuthenticatedLayout (`app-shell`)
**Seletor:** `app-shell`  
**Localização:** `src/app/layout/shell/`

Layout autenticado completo: `mat-sidenav-container` com sidebar, header e `router-outlet`.

**Responsividade:**
- Desktop: sidebar persistente (`mode="side"`)
- Mobile: sidebar drawer (`mode="over"`), fecha ao navegar

---

### 9. PublicLayout (`app-public-layout`)
**Seletor:** `app-public-layout`  
**Localização:** `src/app/layout/public-layout/`

Layout para páginas públicas (login, register). Centraliza conteúdo verticalmente, fundo cinza claro, sem sidebar/topbar.

---

## Responsiveness

### Breakpoints Utilizados

| Breakpoint | Range | Uso |
|------------|-------|-----|
| Mobile | < 768px | Drawer sidebar, cards 1 coluna, layout empilhado |
| Tablet | 768px – 1023px | Sidebar recolhível, cards 2 colunas, painéis empilhados se necessário |
| Desktop | ≥ 1024px | Sidebar persistente, 4 KPIs em linha, gráfico + painel lateral |
| Wide | ≥ 1920px | Container com max-width confortável, não estica indiscriminadamente |

### Grid de KPI Cards
```scss
.kpi-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--space-4);

  @media (min-width: 600px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (min-width: 1024px) {
    grid-template-columns: repeat(4, 1fr);
  }
}
```

### Grid de Painéis (Dashboard)
```scss
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--space-6);

  @media (min-width: 1024px) {
    grid-template-columns: 1.8fr 1fr; // ~65% / 35%
  }
}
```

---

## Accessibility

### Regras Obrigatórias

1. **Contraste WCAG AA** — Todas as combinações de texto/fundo testadas
2. **Foco Visível** — `:focus-visible` em todos os elementos interativos
3. **Navegação por Teclado** — Tab order lógico, sidebar/drawer navegáveis
4. **ARIA Labels** — Ícones sem texto têm `aria-label` ou `matTooltip`
5. **Estados não apenas por cor** — Ícones + texto para sucesso/erro/aviso
6. **Landmarks** — `<nav>`, `<main>`, `<header>`, `<aside>` semânticos
7. **Reduced Motion** — Respeita `prefers-reduced-motion`

### Sidebar Keyboard Navigation
- `Tab` / `Shift+Tab`: navega entre itens
- `Enter` / `Space`: expande submenu ou navega link
- `Escape`: fecha drawer em mobile

### Formulários
- Labels associados via `<mat-label>`
- Erros via `<mat-error>` com `aria-describedby`
- Estados disabled respeitados

---

## Motion

### Transições
```scss
--transition-fast: 150ms ease;
--transition-normal: 250ms ease;
--transition-slow: 350ms ease;
```

### Animações Aplicadas
- Sidebar collapse/expand: 250ms
- Drawer open/close: 250ms
- Hover cards: 200ms (transform + shadow)
- Menu expansion: 200ms (altura)
- Button interactions: 150ms

Todas respeitam `@media (prefers-reduced-motion: reduce)`.

---

## Performance

### Princípios
- Zero bibliotecas UI adicionais além do Angular Material
- Zero bibliotecas de animação
- Zero bibliotecas de ícones extras (usa Material Icons)
- CSS variables para theming (sem runtime JS)
- Lazy loading de rotas (dashboard, auth, etc.)
- OnPush change detection onde aplicável

### Bundle Budgets (angular.json)
- Initial: 600 kB (warning)
- Component styles: 4 kB (warning)
- Atualmente dentro dos limites com margem

---

## Usage Guidelines

### Para Desenvolvedores

1. **Sempre use design tokens** — Nunca hardcode cores, espaçamentos, raios ou sombras
2. **Componentes compartilhados** — Use `PageHeader`, `StatCard`, `ContentPanel`, `EmptyState`, `Breadcrumb` em vez de reimplementar
3. **Layouts corretos** — Páginas privadas usam `AuthenticatedLayout` (via `app-shell`); públicas usam `PublicLayout`
4. **Responsividade** — Teste em 360px, 768px, 1280px, 1920px
5. **Acessibilidade** — Mantenha labels, contraste, foco visível

### Estrutura de Estilos (SCSS)
```
src/
├── styles.scss           # Tokens globais, reset, utilitários
├── app/
│   ├── layout/
│   │   ├── sidebar/
│   │   ├── header/
│   │   ├── shell/
│   │   └── public-layout/
│   ├── shared/ui/
│   │   ├── page-header/
│   │   ├── stat-card/
│   │   ├── content-panel/
│   │   ├── empty-state/
│   │   └── breadcrumb/
│   └── features/
│       ├── dashboard/
│       └── auth/pages/
```

---

## Version
1.0.0 — TASK-UI-1 (Outubro 2026)