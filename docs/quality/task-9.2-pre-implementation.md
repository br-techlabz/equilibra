# TASK-9.2 — PRE-IMPLEMENTATION REPORT: FRONTEND DE RECONCILIAÇÃO DE SALDO

## Status

**PREPARED — pronto para implementação após aprovação do escopo.**

Este relatório atende à etapa obrigatória anterior à codificação da TASK-9.2. Nenhum código foi implementado nesta etapa.

## Contexto Inspecionado

Foram considerados:
- `CLAUDE.md` — regras de UI/Design System (tokens, AuthenticatedLayout, responsividade, acessibilidade);
- `docs/adr/0012-balance-reconciliation-architecture.md` — regras de negócio da reconciliação;
- `docs/quality/task-9.1-report.md` — contrato REST do backend (`POST /asset-accounts/{id}/reconcile`);
- Frontend existente:
  - `asset-accounts.page.ts` — listagem de contas com menu de ações (Editar, Desativar/Reativar);
  - `asset-accounts-api.service.ts` — serviço HTTP para contas;
  - `asset-account.models.ts` — modelos TypeScript (`AssetAccount`, `AssetAccountRequest`);
  - `app.routes.ts` — roteamento com lazy loading e `authGuard`;
  - Design System: `PageHeaderComponent`, `ContentPanelComponent`, `EmptyStateComponent`, tokens CSS.

## Objetivo da Implementação

Adicionar a funcionalidade de **Reconciliação de Saldo** ao módulo de Contas de Ativo:
1. **Ação "Reconciliar"** no menu de cada conta elegível (apenas `AssetAccount` ativas, não cartões de crédito).
2. **Rota dedicada**: `/accounts/:accountId/reconcile` (lazy loaded, protegida por `authGuard`).
3. **Tela de Reconciliação** com:
   - Título "Reconciliar Saldo" + nome da conta;
   - Campo **Data** (date picker, padrão: hoje, não permite futuro);
   - Campo **Saldo** (input monetário pt-BR, obrigatório, 2 casas decimais);
   - Exibição do **saldo calculado de referência** (buscado via API ao mudar a data — opcional se houver endpoint de simulação; caso contrário, só exibido no resultado);
   - Botões **Cancelar/Voltar** e **Confirmar Reconciliação** (com loading e anti-duplo-clique);
   - Confirmação explícita de que será criado lançamento "Ajuste de Saldo" se houver diferença.
4. **Tela de Resultado** pós-sucesso mostrando:
   - Saldo de referência (calculado);
   - Saldo informado;
   - Valor ajustado (diferença);
   - Tipo de lançamento (RECEITA/DESPESA) ou "Sem ajuste necessário";
   - Data reconciliada.
5. Integração com serviços existentes para atualizar listagem/histórico.

## Especificação Técnica

### 1. Modelos TypeScript (novos/atualizados)

**`frontend/src/app/features/asset-accounts/models/reconciliation.models.ts`** (novo):
```typescript
export interface ReconcileAccountRequest {
  reconciliationDate: string; // ISO date (yyyy-MM-dd)
  informedBalance: number;
}

export interface ReconciliationResultResponse {
  accountId: string;
  reconciliationDate: string;
  calculatedBalance: number;
  informedBalance: number;
  difference: number;
  adjustmentType: 'INCOME' | 'EXPENSE' | 'NONE';
  createdTransactionId: string | null;
}
```

**`asset-account.models.ts`** (estender `AssetAccount` se necessário — opcional).

### 2. Serviço de API (atualizar `asset-accounts-api.service.ts`)

Adicionar métodos:
- `reconcile(accountId: string, request: ReconcileAccountRequest): Observable<ReconciliationResultResponse>`
- Opcional: `getBalanceAt(accountId: string, date: string): Observable<{ balance: number }>` — para preview (se backend expor).

### 3. Componente de Reconciliação (novo)

**Rota**: `/accounts/:accountId/reconcile` — lazy loaded em `app.routes.ts`.

**Componente**: `ReconcileAccountPageComponent` (standalone) em:
`frontend/src/app/features/asset-accounts/pages/reconcile-account.page.ts`

**Template** (resumo):
```html
<main class="reconcile-page">
  <app-page-header title="Reconciliar Saldo" subtitle="{{ accountName }}" [actions]="backAction" />
  
  <app-content-panel>
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <!-- Data -->
      <mat-form-field appearance="outline">
        <mat-label>Data da reconciliação</mat-label>
        <input matInput [matDatepicker]="picker" formControlName="reconciliationDate" />
        <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
        <mat-datepicker #picker></mat-datepicker>
        <mat-error *ngIf="form.controls.reconciliationDate.hasError('required')">Informe a data.</mat-error>
        <mat-error *ngIf="form.controls.reconciliationDate.hasError('futureDate')">Não é permitido reconciliar datas futuras.</mat-error>
      </mat-form-field>

      <!-- Saldo de referência (se disponível via preview) -->
      @if (referenceBalance() !== null) {
        <div class="reference-balance">
          <span class="label">Saldo calculado na data</span>
          <strong>{{ formatMoney(referenceBalance()) }}</strong>
        </div>
      }

      <!-- Saldo informado -->
      <mat-form-field appearance="outline">
        <mat-label>Saldo informado (extrato)</mat-label>
        <input matInput formControlName="informedBalance" inputmode="decimal" placeholder="0,00" />
        <mat-hint>Use até duas casas decimais.</mat-hint>
        <mat-error *ngIf="form.controls.informedBalance.hasError('required')">Informe o saldo.</mat-error>
        <mat-error *ngIf="form.controls.informedBalance.hasError('pattern')">Informe um valor válido.</mat-error>
      </mat-form-field>

      <!-- Aviso -->
      <p class="reconcile-notice" role="note">
        <mat-icon aria-hidden="true">info</mat-icon>
        Se houver diferença, o sistema criará uma <strong>receita</strong> (saldo informado maior) ou <strong>despesa</strong> (saldo informado menor) 
        com descrição <em>"Ajuste de Saldo"</em> na categoria dedicada.
      </p>

      <div class="form-actions">
        <button mat-button type="button" routerLink="/accounts">Cancelar</button>
        <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || isSubmitting()">
          @if (isSubmitting()) { <mat-spinner diameter="20"></mat-spinner> Reconciliando... } @else { Confirmar Reconciliação }
        </button>
      </div>
    </form>
  </app-content-panel>
</main>
```

**Estados**: loading (busca de preview), submetendo, erro (Problem Details), sucesso (navega para tela de resultado ou exibe modal de resultado).

### 4. Tela de Resultado

Pode ser:
- **Opção A**: Modal/dialog sobre a tela de reconciliação (mais fluido).
- **Opção B**: Página separada `/accounts/:accountId/reconcile/result` com parâmetros de query ou state.
- **Opção C**: Snackbar/Toast com detalhes + navegação de volta à listagem.

Recomendação: **Opção A** (Dialog `MatDialog` com `ReconciliationResultDialogComponent`) — mantém contexto, não precisa de rota extra.

### 5. Integração na Listagem (`asset-accounts.page.ts`)

- Adicionar item "Reconciliar" no `mat-menu` (`accountMenu`) — apenas se `account.active === true`.
- Handler: `router.navigate(['/accounts', account.id, 'reconcile'])`.

### 6. Roteamento (`app.routes.ts`)

Adicionar rota filha sob `shell`:
```typescript
{
  path: 'accounts/:accountId/reconcile',
  loadComponent: () =>
    import('./features/asset-accounts/pages/reconcile-account.page').then(m => m.ReconcileAccountPageComponent),
},
```

### 7. Validadores Customizados

- `futureDateValidator`: rejeita datas > hoje (considerar timezone do usuário — usar `LocalDate` no backend).
- `moneyPatternValidator`: padrão pt-BR (permite vírgula/ponto, máx. 2 casas decimais).

### 8. Formatação Monetária

- Input: aceita `1.234,56` ou `1234.56` → normaliza para número.
- Exibição: `Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })`.

### 9. Tratamento de Erros

| Status | Ação |
|--------|------|
| 400 | Exibir erro de validação (data futura, precisão, etc.) |
| 401 | Redirect para login (interceptor existente) |
| 404 | "Conta não encontrada" — voltar à listagem |
| 409 | "Conta inativa" — toast + voltar |
| 5xx | "Erro inesperado" — botão retry |

### 10. Acessibilidade

- `aria-label` nos botões de ação do menu.
- `role="alert"` para mensagens de erro.
- Foco visível (`:focus-visible` via tokens).
- Navegação por teclado (Tab, Enter, Esc).
- `mat-datepicker` acessível nativamente.

## Arquivos Previstos

### Novos
1. `frontend/src/app/features/asset-accounts/models/reconciliation.models.ts`
2. `frontend/src/app/features/asset-accounts/pages/reconcile-account.page.ts`
3. `frontend/src/app/features/asset-accounts/pages/reconcile-account.page.scss`
4. `frontend/src/app/features/asset-accounts/pages/reconciliation-result-dialog.component.ts` (opcional)
5. `frontend/src/app/features/asset-accounts/pages/reconciliation-result-dialog.component.scss` (opcional)

### Modificados
6. `frontend/src/app/features/asset-accounts/models/asset-account.models.ts` (imports/exports)
7. `frontend/src/app/features/asset-accounts/data-access/asset-accounts-api.service.ts`
8. `frontend/src/app/features/asset-accounts/pages/asset-accounts.page.ts` (menu + handler)
9. `frontend/src/app/features/asset-accounts/pages/asset-accounts.page.scss` (se necessário)
10. `frontend/src/app/app.routes.ts` (nova rota)

## Testes

- **Unitários** (Jest): validadores, formatação, serviço.
- **Componentes** (Jest + Testing Library): render, interações, estados.
- **E2E** (Playwright): fluxo completo listagem → reconciliação → resultado.
- **Lint**: `npm run lint`.
- **Build**: `npm run build --configuration=production`.

---

**READY FOR TASK-9.2 IMPLEMENTATION**