# TASK-4.6 — Interface de Anexos

## Status

**CONCLUÍDA**

## Implementado

- `AttachmentService` Angular tipado;
- `TransactionAttachmentsComponent` compartilhado entre Expense, Income e Transfer;
- seleção de arquivos com whitelist visual PDF/JPEG/PNG/WEBP;
- validação antecipada de arquivo vazio e limite de 10 MB;
- upload multipart usando FormData;
- progresso/estado de upload;
- listagem, empty state e carregamento;
- download por HttpClient autenticado, Blob e Object URL revogado;
- remoção com confirmação;
- accessible names para ações;
- modo somente leitura para transações canceladas;
- integração nos três drawers financeiros;
- nenhum JWT, storageKey ou path físico exposto;
- nenhuma alteração no ledger, Dashboard ou Histórico.

## Verificação

- frontend lint: PASS;
- testes Angular: 27 SUCCESS;
- frontend build: PASS;
- `git diff --check`: PASS.

O build mantém apenas warnings de orçamento SCSS preexistentes.

## Fora de escopo

Preview de arquivos, drag-and-drop, S3/MinIO, scanner, OCR, thumbnails, filtro do Histórico, Dashboard e qualquer alteração backend.
