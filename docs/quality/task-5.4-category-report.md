# TASK-5.4 — INTERFACE DO RELATÓRIO POR CATEGORIA

## Status

**CONCLUÍDA**

## Route and API

- Rota privada: `/reports/categories`.
- Sidebar: Relatórios → Por categoria ativo; Auditoria permanece futuro/desabilitado.
- Serviço tipado consome exclusivamente `GET /api/reports/categories`.
- Request usa from/to ISO e accountIds repetidos quando há seleção explícita.
- Nenhuma regra financeira é recalculada no Angular.

## Interface

- PageHeader, filtros de período/contas, Apply/Restore e estados de loading/error/retry.
- Indicadores exibem incomeTotal, expenseTotal e netResult diretamente do backend.
- Tabela exibe categoria, applicability, receitas, despesas e resultado.
- Category BOTH mantém receitas e despesas em colunas separadas.
- Categorias inativas podem ser exibidas pelo backend; não são filtradas pelo frontend.
- Não há filtro fictício de categoryIds/tagIds porque a API 5.3 não os suporta.
- Não foi adicionada biblioteca gráfica; a tabela é a visualização analítica acessível e suficiente para o DTO atual.
- Mobile usa cards por categoria sem overflow horizontal global.

## Validação

- `npm run lint`: **PASS**.
- `npx ng build --configuration development`: **PASS**.
- `git diff --check`: **PASS**.

## Fora de escopo

Exportação, gráficos adicionais, auditoria, backend, migrations, filtros não suportados e TASK-5.5.

**READY FOR TASK-5.5 — Backend do Relatório de Auditoria**
