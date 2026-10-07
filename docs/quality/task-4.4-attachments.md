# TASK-4.4 — Infraestrutura Segura de Anexos

## Status

**CONCLUÍDA**

## Implementado

- metadata `TransactionAttachment` com UUID, owner, transaction, nome original, storage key, MIME, tamanho, SHA-256, timestamp e version;
- migration Flyway V8 `transaction_attachments` com FKs, índices e unique storage key;
- repository owner-aware para metadata e contagem por transação;
- `AttachmentProperties` centralizado e configurável;
- porta `AttachmentStorage`;
- implementação local `LocalFileSystemAttachmentStorage` com root configurável, temp write/move, no overwrite, delete idempotente e proteção contra escape do root;
- `AttachmentFilePolicy` com limite de tamanho, whitelist PDF/JPEG/PNG/WEBP, magic bytes e checksum SHA-256;
- configuração habilitada via `@EnableConfigurationProperties` e propriedades `equilibra.attachments.*`;
- nenhuma pasta de storage exposta como static resource;
- nenhum endpoint de upload/download/delete e nenhuma UI implementados, conforme o escopo da TASK-4.4.

## Segurança

A infraestrutura rejeita arquivos vazios, MIME não permitido, assinaturas incompatíveis, nomes com path traversal, CR/LF, NUL ou controle; storage keys são internos e não usam o nome original. O checksum é calculado no servidor. Bytes, paths e conteúdo não são expostos por API.

## Testes

- `AttachmentFilePolicyTest`: 4 testes, 0 falhas;
- `LocalFileSystemAttachmentStorageTest`: 1 teste, 0 falhas;
- `AttachmentMigrationTest`: 1 teste MySQL/Testcontainers, 0 falhas;
- `./mvnw verify`: suíte completa aprovada após atualização das expectativas de migration V8;
- frontend lint/test/build: regressão preservada;
- `git diff --check`: PASS.

## Consistência DB × storage

A porta de storage permite a futura orquestração `store → metadata` com compensação em falha de persistência. Não há transação distribuída nem scheduler de órfãos nesta task; esse risco está documentado para a implementação funcional da TASK-4.5.

## Fora de escopo

- endpoints funcionais de upload/download/delete;
- UI Angular;
- S3/MinIO;
- scanner de malware, OCR, thumbnails e preview;
- alterações no ledger ou nas invariantes financeiras.
