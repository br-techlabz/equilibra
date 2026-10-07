# TASK-4.2 — Interface de Tags

## Status

**EM IMPLEMENTAÇÃO — validação final pendente.**

## Implementado

- models tipados `Tag` e `TagRequest`;
- `TagsApiService` consumindo a API real da TASK-4.1;
- rota privada lazy `/tags`;
- item simples Tags na Sidebar;
- tela com PageHeader, ContentPanel e EmptyState;
- listagem ativa/inativa com `includeInactive`;
- estados de loading, empty, erro e retry;
- criação e edição por drawer;
- desativação com confirmação;
- reativação;
- mensagens amigáveis para conflitos 409;
- layout desktop e mobile responsivo;
- status textual acessível.

## Fora de escopo

Não foram implementados associação Tag↔Transaction, Tags em Expense/Income/Transfer, filtro do Histórico, Attachments, upload/download ou Dashboard.

## Validação pendente

- testes unitários dedicados da página e service;
- lint, testes e build final;
- E2E desktop/mobile da tela de Tags;
- relatório final de responsividade e acessibilidade.
