# TASK-5.5 — PRE-IMPLEMENTATION REPORT

## Definição

Auditoria é um read model paginado de `FinancialTransaction` lógica para rastreabilidade. Não é event sourcing: o modelo não guarda versões anteriores nem histórico imutável de alterações.

## Contrato previsto

```text
GET /api/reports/audit
```

`from`/`to` obrigatórios, `accountIds` repetidos, `type`, `status`, `categoryId`, `tagIds`, `page`, `size`. Status ausente será ALL, diferentemente do History ACTIVE default, para preservar rastreabilidade. Período usa occurredAt e `[from,to)`.

## Campos

DTO explícito conterá ID, type, status, description, occurredAt, createdAt, updatedAt, amount, source/destination, categoryId, notes, tags e attachmentCount. Nenhuma entidade JPA, storage key, path, bytes ou JWT será exposta.

## Contas e transferências

AccountIds selecionam source OR destination e aceitam contas inativas próprias. Transfer aparece uma vez mesmo com source e destination selecionados. IDs estrangeiros/inexistentes são 404; IDs duplicados são deduplicados.

## Query e performance

Paginação e ordenação `occurredAt DESC, id DESC` no banco. A query será owner-scoped; tags/attachments serão enriquecidos em batch após a página, sem joins multiplicadores. A infraestrutura do History será reutilizada apenas onde compatível; seu default ACTIVE não será alterado.

## Testes

Testcontainers/MySQL cobrirá auth, ownership, status ALL/ACTIVE/CANCELLED, período boundaries, contas, tipos, categoria/tags, attachmentCount, paginação, totalElements, ordem estável, transfer once, campos seguros e consistência com History.

## Arquivos

Criar módulo `backend/src/main/java/br/com/equilibra/report/audit/`, testes dedicados e relatório final. Não alterar frontend, migrations ou implementar TASK-5.6.
