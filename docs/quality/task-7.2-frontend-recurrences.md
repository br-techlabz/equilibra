# TASK-7.2 — Frontend de Recorrências

## Status

**CONCLUÍDA** — implementação integrada ao contrato real da TASK-7.1 e suíte E2E global aprovada.

## 1. ADR e contratos

Utilizado `docs/adr/0006-financial-predictability-architecture.md`. A interface consome apenas `GET/POST/PUT /recurrences`, `GET /recurrences/{id}` e as transições `pause`, `resume`, `end` e `cancel`. Não envia `ownerId`.

Os modelos refletem `EXPENSE|INCOME`, `MONTHLY|YEARLY` e `ACTIVE|PAUSED|ENDED|CANCELLED`. Filtros de tipo e estado são locais, pois a API atual não possui parâmetros de filtro.

## 2. Implementação

- Rota privada `/recurrences` no shell autenticado.
- Item `Recorrências` com ícone `repeat` na sidebar.
- Listagem responsiva com descrição, tipo, valor BRL, periodicidade, vencimento, vigência, categoria, conta, estado e menu de ações.
- Drawer para criação e edição; edição envia somente campos aceitos pelo DTO `Update`.
- Seletores de categorias e contas reutilizando serviços existentes.
- Estados de carregamento, vazio, erro, retry e submissão protegida contra duplicidade.
- Confirmações para pausar, reativar, encerrar e cancelar.
- Datas enviadas como `YYYY-MM-DD`, sem conversão por timezone.
- Nenhuma geração de ocorrência, efetivação, alteração de saldo ou cálculo financeiro.

## 3. Arquivos

Criados:

- `frontend/src/app/features/recurrences/models/recurrence.models.ts`
- `frontend/src/app/features/recurrences/data-access/recurrence-api.service.ts`
- `frontend/src/app/features/recurrences/pages/recurrences.page.ts`
- `frontend/src/app/features/recurrences/pages/recurrences.page.html`
- `frontend/src/app/features/recurrences/pages/recurrences.page.scss`

Modificados:

- `frontend/src/app/app.routes.ts`
- `frontend/src/app/layout/sidebar/sidebar.component.ts`

## 4. Validação

- `cd frontend && npm run lint`: aprovado.
- `cd frontend && npm run build`: aprovado; apenas avisos de budgets já existentes em outros componentes e aviso existente de optional chaining.
- `cd frontend && npm test -- --watch=false --browsers=ChromeHeadless --progress=false --code-coverage=false --poll=0`: Karma iniciou e conectou ao ChromeHeadless, mas não apresentou resumo final; após 120 segundos o timeout encerrou o processo, portanto não foi aprovado.
- `cd frontend && npm run e2e -- --project=desktop e2e/recurrences.spec.ts --reporter=list`: aprovado; 1 teste passou, cobrindo listagem, filtro e ausência de `ownerId`.
- Ajustados testes existentes para navegação autenticada sem recarregar a aplicação, seletores de datas, navegação mobile, interceptações e matcher incompatível `toBeFalse`.
- Projeto tablet passou a usar Chromium em viewport 768x1024, evitando dependência de WebKit no ambiente.
- `cd frontend && npm run e2e -- --reporter=line`: aprovado; 33 testes passaram em 2,7 minutos com backend/frontend ativos.
- `npm run lint`: aprovado.

## 5. Pendências e limites conhecidos

A API da TASK-7.1 não retorna nomes de categoria/conta nem suporta histórico, ocorrências ou filtros server-side. A tela usa as listas existentes e exibe o ID como fallback. Essas limitações não foram compensadas com contratos fictícios.

Não foram alterados ledger, transações, contas, categorias ou regras financeiras.
