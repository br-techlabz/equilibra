# TASK-4.7 — Integração de Tags e Anexos no Histórico

## Status

**CONCLUÍDA**

## Implementado

- Histórico preparado com `tags` e `attachmentCount`;
- filtro server-side por múltiplos `tagIds`, semântica ANY e validação owner-scoped;
- contagem batch de anexos por página, sem abrir bytes/storage;
- summaries reais de Tags ordenados por nome;
- frontend preparado para serialização de múltiplos Tag IDs e exibição compacta de metadata;
- painel readonly de anexos reutiliza `TransactionAttachmentsComponent`;
- nenhum storage key/path ou byte é exposto;
- paginação e Transfer lógica preservadas.

## Verificações

- compilação backend: PASS;
- `GoldenScenarioIntegrationTest`: 2 testes, 0 falhas;
- `TagControllerIntegrationTest`: 3 testes, 0 falhas;
- `AttachmentControllerIntegrationTest`: 3 testes, 0 falhas;
- frontend lint: PASS;
- testes Angular: 27 SUCCESS;
- frontend build: PASS;
- E2E de navegação do Histórico: PASS;
- `git diff --check`: PASS.

## Fora de escopo

Upload/delete pelo Histórico, novo storage, Dashboard por metadata e CRUD financeiro genérico.
