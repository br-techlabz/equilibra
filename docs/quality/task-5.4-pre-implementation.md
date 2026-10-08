# TASK-5.4 — PRE-IMPLEMENTATION REPORT

## API real

`GET /api/reports/categories` exige `from`/`to` ISO date-time e aceita `accountIds` repetidos opcionalmente. Ausência de IDs significa todas as contas próprias. O response contém `incomeTotal`, `expenseTotal`, `netResult` e grupos com `categoryId`, `categoryName`, `applicability`, `incomeTotal`, `expenseTotal`, `netResult`.

Não existem filtros reais de categoryIds/tagIds neste endpoint; a interface não os inventará.

## Semântica

Valores são integralmente do backend. Category BOTH exibe receitas e despesas em colunas separadas. Categorias inativas presentes no histórico são exibidas e recebem indicação textual. Transferências não aparecem como receita/despesa.

## Layout e componentes

Authenticated Shell → PageHeader → filtros → cards de indicadores → tabela analítica. Em mobile, a tabela vira cards/lista sem overflow. Serão reutilizados PageHeader, ContentPanel, EmptyState, Material controls e tokens.

A biblioteca ECharts não está presente no frontend atual; não será adicionada uma dependência nova nesta task. A tabela analítica é a representação principal acessível e suficiente para o DTO real.

## Estado e filtros

Draft e applied filters serão separados. Default é o mês atual e todas as contas. Aplicar dispara request; restaurar retorna ao default. Loading não exibe zeros falsos; erro possui retry; nenhum grupo/conta produz empty state distinto.

## Testes

Service: serialização ISO, accountIds ausentes/repetidos. Componente: apply/reset, período inválido sem request, loading/success/empty/error, valores negativos, BOTH/inativas e ausência de cálculo paralelo. E2E: rota privada, request real, indicadores/tabela e mobile sem overflow.

## Arquivos

Criar models/service/page e relatórios quality; modificar rota e sidebar para ativar `/reports/categories`, mantendo Auditoria disabled. Nenhuma alteração backend ou exportação.
