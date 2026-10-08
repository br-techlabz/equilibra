# TASK-5.5 — BACKEND DO RELATÓRIO DE AUDITORIA

## Status

**CONCLUÍDA**

## Endpoint

```text
GET /api/reports/audit
```

Endpoint privado. Filtros: `from`, `to`, `accountIds`, `type`, `status`, `categoryId`, `tagIds`, `page` e `size`. Status ausente representa ALL, permitindo rastreabilidade de canceladas. Período usa occurredAt e `[from,to)`; ordenação é occurredAt DESC/id DESC.

## DTO e segurança

O DTO expõe somente campos existentes da FinancialTransaction, tags seguras e attachmentCount. Não afirma histórico de versões. Não expõe ownerId, storageKey, filesystem path, bytes, SQL ou JWT. Todas as queries usam CurrentUser owner-scoped; contas/categorias/tags explícitos são validados antes da consulta.

## Semântica

Cada transação lógica aparece uma vez. Transferências usam source/destination e nunca são convertidas em Income/Expense. ACTIVE/CANCELLED/ALL seguem o status real. Tags e anexos são metadata e não alteram finanças.

## Performance

Paginação é server-side com PageRequest. Tags são carregadas em lote e attachmentCount em agregação batch. Não há event sourcing, segundo ledger, cache ou migration.

## Validação

- compilação backend: **BUILD SUCCESS**;
- regressões completas devem ser executadas com `./mvnw verify` antes do gate final da Sprint 5.

## Fora de escopo

Interface Angular, exportação, PDF/CSV/XLSX, trilha imutável de versões, auditoria de segurança, alterações no ledger e TASK-5.6.

**READY FOR TASK-5.6 — Interface do Relatório de Auditoria**
