# TASK-4.5 — API e Ciclo de Vida de Anexos

## Status

**CONCLUÍDA**

## Implementado

- `AttachmentResponse` sem storage key/path;
- `AttachmentService` com upload, list, find, streaming open e delete;
- `AttachmentController` privado para POST multipart, listagem, download e delete;
- ownership via `CurrentUser` e transaction/attachment owner-aware;
- limite de anexos por transação;
- validação central da TASK-4.4;
- storage-first e compensação best-effort em falha de metadata;
- download com Content-Disposition attachment, `nosniff` e `Cache-Control: private/no-store`;
- suporte à FinancialTransaction lógica, incluindo Expense, Income e Transfer;
- nenhuma alteração financeira.

## Testes executados

- `AttachmentControllerIntegrationTest`: 3 testes MySQL/Testcontainers, 0 falhas;
- upload, list, download streaming, delete e headers privados;
- IDOR de transaction e validação de assinatura inválida;
- `AttachmentFilePolicyTest`: 4 testes;
- `LocalFileSystemAttachmentStorageTest`: 1 teste;
- `AttachmentMigrationTest`: 1 teste;
- regressões financeiras e Tags selecionadas passaram;
- frontend lint, 27 testes Angular e build passaram;
- `git diff --check`: PASS.

## Consistência

Upload grava bytes antes da metadata e remove o objeto se a persistência falhar. Falhas de storage não criam metadata. O risco residual de compensação filesystem/DB não ser distribuída é documentado para evolução futura.

## Fora de escopo

UI Angular, preview, progresso, S3/MinIO, scanner de malware, OCR, thumbnails e filtros do Histórico/Dashboard.
