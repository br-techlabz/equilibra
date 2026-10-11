# TASK-8.2 — PRE-IMPLEMENTATION REPORT

## Status

**PREPARED — pronto para implementação após aprovação do escopo.**

Nenhum código foi alterado nesta etapa.

## Contexto inspecionado

Foram inspecionados:

- `CLAUDE.md` e regras de UI/design system;
- `docs/adr/0011-credit-card-architecture.md`;
- `docs/quality/task-8.1-backend.md`;
- rotas em `frontend/src/app/app.routes.ts`;
- shell autenticado e sidebar recolhível;
- tokens em `frontend/src/styles.scss`;
- serviço e página de contas de ativo;
- interceptors de autenticação, request ID e tratamento de erros;
- padrões de serviços HTTP, formulários reativos, Material e testes.

A TASK-8.1 fornece APIs reais em `/credit-cards`, sem necessidade de `ownerId` no frontend.

## Objetivo

Criar a experiência autenticada de cadastro e administração de cartões de crédito, consumindo exclusivamente a API real da TASK-8.1 e sem implementar compras, parcelas, faturas ou pagamentos.

## Arquitetura frontend prevista

Criar o feature module por arquivos standalone:

```text
frontend/src/app/features/credit-cards/
├── data-access/credit-cards-api.service.ts
├── models/credit-card.models.ts
└── pages/credit-cards.page.ts
```

A página será carregada lazy dentro do `ShellComponent`, protegida pelo `authGuard` já aplicado ao agrupamento autenticado.

Não criar estado global ou persistência local do cartão. O estado ficará no componente/serviço da tela e será sempre atualizado pela API.

## Rotas e navegação

Adicionar em `app.routes.ts`:

```text
/credit-cards
```

A rota deverá carregar lazy:

```typescript
import('./features/credit-cards/pages/credit-cards.page')
```

Adicionar à `SidebarComponent.navigationItems`:

```text
Cartões de Crédito -> /credit-cards
```

Ícone recomendado: `credit_card`.

O shell autenticado existente já fornece sidebar, header, breadcrumb e `router-outlet`. Não criar layout paralelo.

## Contrato frontend/backend

### Modelo

```typescript
export type CreditCardStatus = 'ACTIVE' | 'INACTIVE';

export interface CreditCard {
  id: string;
  name: string;
  institution: string;
  nickname: string | null;
  totalLimit: number;
  committedLimit: number;
  reservedLimit: number;
  availableLimit: number;
  closingDay: number;
  dueDay: number;
  timezone: string;
  status: CreditCardStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreditCardRequest {
  name: string;
  institution: string;
  nickname?: string | null;
  totalLimit: number;
  closingDay: number;
  dueDay: number;
  timezone?: string | null;
}
```

### Endpoints utilizados

```text
GET    /credit-cards?includeInactive=false
POST   /credit-cards
PUT    /credit-cards/{id}
PATCH  /credit-cards/{id}/deactivate
PATCH  /credit-cards/{id}/activate
```

Não enviar `ownerId`. Não inventar endpoints para compras, faturas ou pagamentos.

### Indicadores

A API da TASK-8.1 retorna:

```text
committedLimit = 0
reservedLimit = 0
availableLimit = totalLimit
```

A interface deverá exibir esses valores reais, sem calcular consumo fictício localmente.

## Página “Meus Cartões”

A página deverá reutilizar:

- `PageHeaderComponent`;
- `ContentPanelComponent`;
- `EmptyStateComponent`;
- Angular Material;
- tokens de `styles.scss`.

### Card responsivo

Cada cartão deverá mostrar:

- nome;
- instituição;
- apelido quando informado;
- limite total em BRL;
- disponível;
- comprometido, quando retornado;
- dia de fechamento;
- dia de vencimento;
- status ativo/inativo;
- menu de ações.

O layout deverá funcionar em 360px, 390px, tablet e desktop, sem overflow horizontal.

### Estados

Implementar explicitamente:

- loading com indicador Material;
- lista vazia com ação “Novo cartão”;
- erro de carregamento com mensagem e ação “Tentar novamente”;
- estado de sucesso após cadastro/edição/desativação/reativação;
- erro de formulário inline;
- confirmação antes de desativar;
- feedback por `MatSnackBar` ou padrão equivalente existente.

O detalhe poderá ser um drawer/modal ou seção da própria página, desde que mostre dados reais e não simule faturas inexistentes. A recomendação é reutilizar o drawer lateral adotado em `AssetAccountsPageComponent` para manter consistência.

## Formulário reativo

Campos:

- `name`: required, máximo 100;
- `institution`: required, máximo 100;
- `nickname`: opcional, máximo 100;
- `totalLimit`: required, positivo, máximo 2 casas;
- `closingDay`: required, inteiro entre 1 e 31;
- `dueDay`: required, inteiro entre 1 e 31;
- `timezone`: opcional, default `America/Sao_Paulo`, máximo 64.

O frontend deve enviar número decimal normalizado, não string formatada com `R$`. A máscara/apresentação pode usar pt-BR, mas a conversão para payload deve preservar centavos sem inventar regras financeiras.

Não exigir `dueDay > closingDay`; o backend aceita ambos independentemente e a regra de calendário pertence às faturas futuras.

Mensagens de validação devem ser acessíveis via `mat-error`, com `aria-live` para erros de operação.

## Serviço HTTP

`CreditCardsApiService` deverá:

- usar `HttpClient` e `environment.apiUrl`;
- expor métodos tipados para os cinco fluxos;
- não armazenar JWT, owner ou cartão em `localStorage/sessionStorage`;
- não manter dados duplicados fora do estado da tela;
- propagar `HttpErrorResponse` para a página tratar Problem Details.

Evitar chamadas concorrentes obsoletas em alterações rápidas de estado. Para a primeira página, o filtro é somente `includeInactive`; carregamentos deverão ser serializados/cancelados ou protegidos contra resposta antiga sobrescrever resposta nova, preferencialmente via `switchMap` se o serviço reativo for adotado.

## Tratamento de erros

Mapear conforme contrato HTTP:

- `401`: interceptor/auth flow existente;
- `403`: mensagem de acesso negado;
- `404`: cartão não encontrado, atualizar lista quando apropriado;
- `409`: conflito de operação, especialmente atualização concorrente ou estado inválido;
- `400`/Problem Details: mensagens de campos quando disponíveis;
- demais erros: mensagem genérica e retry.

Não exibir stack trace, payload sensível ou detalhes internos.

## Acessibilidade e responsividade

- landmarks semânticos: `main`, headings e regiões de alerta;
- navegação por teclado em cards, menus, drawer e formulário;
- foco visível usando tokens existentes;
- labels associados aos inputs;
- `aria-label` em ações icon-only;
- confirmação acessível para desativação;
- contraste WCAG AA;
- breakpoints existentes e tokens `--bp-*`;
- nenhuma dependência de hover para ações essenciais;
- sidebar recolhível continua funcionando pelo shell existente.

## Arquivos previstos

### Criar

```text
frontend/src/app/features/credit-cards/models/credit-card.models.ts
frontend/src/app/features/credit-cards/data-access/credit-cards-api.service.ts
frontend/src/app/features/credit-cards/pages/credit-cards.page.ts
frontend/src/app/features/credit-cards/pages/credit-cards.page.scss
frontend/src/app/features/credit-cards/data-access/credit-cards-api.service.spec.ts
frontend/src/app/features/credit-cards/pages/credit-cards.page.spec.ts
```

### Modificar

```text
frontend/src/app/app.routes.ts
frontend/src/app/layout/sidebar/sidebar.component.ts
```

Modificar apenas para rota e navegação. Não alterar autenticação ou shell sem necessidade concreta.

## Testes previstos

### Serviço

- URL `/credit-cards` correta;
- listagem com `includeInactive`;
- POST, PUT, deactivate e activate;
- payload não contém `ownerId`;
- erros HTTP são propagados;
- não há persistência em storage.

### Componente

- carrega e renderiza cartões reais;
- renderiza valores em BRL `pt-BR`;
- renderiza dias como “dia 3” e “dia 10”;
- mostra estado vazio;
- mostra loading;
- mostra erro e retry;
- abre formulário de criação;
- valida campos e limite;
- cria cartão usando API;
- edita cartão usando API;
- desativa somente após confirmação;
- reativa cartão inativo;
- mostra erro 409;
- não exibe PAN/CVV/faturas simuladas.

### Navegação

- rota `/credit-cards` é autenticada;
- item aparece na sidebar;
- rota funciona com sidebar expandida e recolhida.

## Golden scenario frontend

Cadastrar:

```text
Cartão Principal
limite: R$ 5.000,00
fechamento: dia 3
vencimento: dia 10
```

A tela deverá:

- enviar somente o contrato permitido;
- exibir o cartão retornado pela API;
- mostrar `R$ 5.000,00` como limite total e disponível;
- mostrar comprometido `R$ 0,00`;
- não criar nem exibir despesas, movimentos, compromissos ou faturas;
- preservar o saldo bancário, pois nenhuma API de conta é chamada.

## Verificação prevista

Executar após implementação:

```bash
cd frontend
npm run lint
npm test -- --watch=false
npm run build
```

Executar E2E configurado para:

- rota autenticada;
- cadastro;
- listagem;
- edição;
- desativação;
- responsividade básica;
- ausência de ownerId e endpoints fora do escopo.

Validar manualmente ou via browser em 360px, 390px, tablet e desktop.

## Riscos e decisões

- **Risco:** frontend duplicar regra financeira. Mitigação: apresentar indicadores retornados pela API e não calcular consumo.
- **Risco:** resposta antiga substituir estado novo. Mitigação: fluxo cancelável/`switchMap` ou token de requisição.
- **Risco:** uso de valores hardcoded. Mitigação: tokens do Design System e componentes compartilhados.
- **Risco:** expor dados sensíveis. Mitigação: modelo não possui PAN/CVV e tela não cria campos para eles.
- **Decisão:** faturas e compras não aparecem como placeholders funcionais nesta task.
- **Decisão:** timezone é editável como texto controlado pelo backend; não criar seletor de timezones sem requisito de produto.

## Gate pré-implementação

Não há decisão essencial pendente para implementar a interface prevista. O backend da TASK-8.1 fornece o contrato necessário.

**READY TO IMPLEMENT TASK-8.2.**
