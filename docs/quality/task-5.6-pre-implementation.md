# TASK-5.6 — PRE-IMPLEMENTATION REPORT

## API real

`GET /api/reports/audit` retorna Page-like `{content,page,size,totalElements,totalPages}`. Cada item possui id, type, status, description, occurredAt, createdAt, updatedAt, amount, categoryId, sourceAccountId, destinationAccountId, notes, tags e attachmentCount.

Parâmetros: from/to, accountIds repetidos, type, status, categoryId, tagIds repetidos, page e size. Status omitido representa ALL. A interface não adicionará filtros inexistentes.

## Semântica

Auditoria mostra transações lógicas, uma vez por item; transferências exibem origem/destino. CANCELLED permanece visível com status textual. Não há histórico de versões, responsável pela alteração ou motivo persistido; esses campos não serão inventados.

## Estrutura

PageHeader, filtros, contador contextual, tabela desktop/cards mobile, paginator e drawer read-only de detalhes. Tags e attachmentCount são exibidos sem requests por linha. Nenhum botão de editar/cancelar/excluir/alterar tags/anexos.

## Estados e concorrência

Draft/applied filters separados; default mês atual, todas as contas, type ALL, status ALL. Loading/error/empty/retry explícitos. Apply reseta página e requests usam estado aplicado; respostas antigas não devem substituir consulta nova.

## Testes

Service: query params, datas, enums, account/tag IDs. Componente: defaults, Apply/Restore, ALL/ACTIVE/CANCELLED, loading/empty/error, paginação, transfer once, detalhes read-only, tags/count e responsividade. E2E real desktop/mobile se backend disponível.

## Arquivos

Criar models/service/page e relatórios de qualidade; modificar rota/sidebar para ativar Auditoria. Não alterar backend, ledger ou exportação.
