# TASK-4.1 — Domínio, Persistência e API de Tags

## Status

**CONCLUÍDA**

## ADR utilizado

`docs/adr/0003-tags-and-attachments.md`.

## Implementação

- `Tag` com UUID, `ownerId`, `name`, `normalizedName`, `active`, timestamps e version;
- nome trim, não vazio, máximo de 100 caracteres e case-insensitive sem remover acentos;
- lifecycle de ativação/desativação;
- migration Flyway V6 para `tags`, FK para `users`, índices e unicidade ativa por owner via chave gerada no MySQL;
- repository owner-aware;
- service transacional com conflitos de unicidade convertidos para 409;
- DTOs explícitos para entrada e saída;
- API privada `/tags` com OpenAPI/security;
- IDOR, owner spoofing e mass assignment protegidos;
- sem associação Tag↔Transaction, UI ou Attachments.

## Endpoints

```text
POST   /api/tags
GET    /api/tags?includeInactive=false
GET    /api/tags/{id}
PUT    /api/tags/{id}
PATCH  /api/tags/{id}/deactivate
PATCH  /api/tags/{id}/activate
```

## Testes

- `TagTest`: 3 testes de domínio;
- `TagRepositoryTest`: 2 testes com MySQL/Testcontainers;
- `TagControllerIntegrationTest`: 3 testes HTTP com autenticação JWT, CRUD, lifecycle, 401, IDOR, unicidade, owner isolation, validação e mass assignment;
- regressão de `CategoryMigrationTest` atualizada para a versão V6 e três migrations ao atualizar de V3;
- `./mvnw verify` executado com a suíte completa após a correção da expectativa de migration.

## Fora de escopo confirmado

- interface Angular;
- associação Tag ↔ FinancialTransaction;
- alterações em Expense, Income ou Transfer;
- filtro do histórico por Tag;
- Attachments, upload e download;
- Dashboard e Tags padrão/seed.
