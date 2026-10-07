# TASK-5.2 — PRE-IMPLEMENTATION REPORT

## API

- Method: `GET`
- Path: `/api/reports/financial`
- Autenticação: JWT via interceptors existentes.
- Backend é a autoridade para todos os valores financeiros.

## Request

- `from`: ISO date-time obrigatório;
- `to`: ISO date-time obrigatório;
- `accountIds`: parâmetro repetido opcional;
- `page`: default 0;
- `size`: default 20, máximo 100.

Ausência de `accountIds` significa todas as contas próprias; contas inativas podem ser selecionadas. A interface não envia ownerId, userId ou token na URL.

## Response real

```text
period { from, to }
accounts [{ id, name, active }]
openingBalance
incomeTotal
expenseTotal
financialResult
incomingTransferTotal
outgoingTransferTotal
internalTransferTotal
balanceChange
closingBalance
details { content, page, size, totalElements, totalPages }
```

Cada detail contém id, type, status, description, occurredAt, amount, categoryId, sourceAccountId, destinationAccountId e notes.

## Semântica financeira

A UI apenas exibe campos do backend:

- resultado financeiro = income − expense;
- variação do saldo também inclui transferências que cruzam a seleção;
- transferências internas têm efeito agregado zero;
- saldo inicial/final são posições das contas selecionadas;
- CANCELLED não produz efeito.

## Seleção de contas

O endpoint existente de Asset Accounts será chamado com `includeInactive=true`. O formulário oferece Todas, uma ou várias contas. Labels usam nome e indicam Inativa; UUID nunca é label principal. A ausência de IDs representa Todas e evita URL com lista desnecessária.

## Componentes reutilizados

- Authenticated Shell/AuthGuard;
- PageHeader;
- ContentPanel;
- Mat form fields/select/multi-select;
- Mat paginator;
- Empty/error/loading patterns das telas de History/Dashboard;
- tokens de `styles.scss`.

## Estrutura proposta

```text
PageHeader
  Filtros do relatório (data inicial, data final, contas, Aplicar, Restaurar)
  resumo principal (saldo inicial, receitas, despesas, resultado, variação, saldo final)
  transferências (internas, recebidas, enviadas)
  detalhes paginados
```

A explicação acessível diferencia Resultado financeiro de Variação do saldo. Sem movimentações, o summary continua visível; sem contas, exibe empty state com CTA para Contas.

## Estado

- initial: filtros padrão do mês atual e contas carregadas;
- loading: spinner/status, sem zeros falsos;
- success: response aplicado;
- empty: details vazios, summary preservado;
- error: mensagem segura e retry com appliedFilters.

`draftFilters` só substitui `appliedFilters` após Aplicar. Request sequencing/switchMap evita resposta antiga sobrescrever a nova.

## Responsividade e acessibilidade

Filtros empilham em mobile; summary usa cards responsivos; detalhes usam tabela desktop e cards mobile. Validar 360, 390, 768, 1024, 1280, 1366 e 1920px sem overflow horizontal. Labels, foco, teclado, status/error anunciáveis e ajuda acessível não dependente somente de hover.

## Testes

- API service: URL, datas ISO, accountIds ausentes/repetidos;
- filtros draft/applied, Apply, Reset, período inválido sem request;
- estados initial/loading/success/empty/error/retry;
- renderização usa valores retornados, sem recálculo;
- transferências exibidas separadamente;
- paginação server-side;
- contas inativas e empty sem contas;
- E2E autenticado quando ambiente real estiver disponível.

## Arquivos

### Criar

- models/service/page/scss de reports;
- rota e documentação final da task;
- testes específicos.

### Modificar

- `app.routes.ts`;
- `sidebar.component.ts` para Relatórios → Financeiro.

### Reutilizar

Interceptors/auth, AssetAccountsApiService, componentes de layout e tokens existentes.
