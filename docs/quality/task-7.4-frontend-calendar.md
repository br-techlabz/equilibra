# TASK-7.4 — Calendário Financeiro

## Status

**CONCLUÍDA**

## Implementado

- Rota autenticada `/financial-calendar`.
- Item `Agenda Financeira` na sidebar.
- Models tipados para `FinancialCommitment`.
- Serviço Angular para listagem, criação, edição, cancelamento e efetivação.
- Navegação mensal.
- Grade compacta de dias com indicadores.
- Agenda ordenada por vencimento retornado pela API.
- Filtros por tipo e status.
- Criação e edição de compromissos pendentes.
- Cancelamento e efetivação usando endpoints únicos do backend.
- Estados de loading, empty e error/retry.
- Formatação BRL e indicação textual de tipo/status/atraso.
- Nenhum request envia `ownerId`.

## Verificação

- `cd frontend && npm run lint`: aprovado.
- `cd frontend && npm run build`: aprovado com avisos preexistentes de orçamento de estilos e optional chaining compartilhado.
- Testes unitários específicos da página/service: adicionados; o runner Karma permanece bloqueado após conectar ao ChromeHeadless e não apresenta resumo.
- E2E específico de calendário: aprovado; 2 testes desktop passaram em 9,7s.
- Validação visual manual: calendário mensal, weekdays, destaque do dia atual e agenda responsiva implementados.

## Limitações conhecidas

- A API não retorna nomes de contas/categorias; a tela usa IDs nos campos do formulário e não inventa enriquecimento.
- O formulário usa IDs de conta/categoria até integração com seletores existentes.
- Não há endpoint de detalhe enriquecido ou calendário dedicado; a grade é derivada da página de compromissos filtrada por intervalo.
- Efetivação usa `prompt` para valor nesta primeira implementação e deve ser refinada em teste/UI posterior.
