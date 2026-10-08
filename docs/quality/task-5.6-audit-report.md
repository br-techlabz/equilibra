# TASK-5.6 — INTERFACE DO RELATÓRIO DE AUDITORIA

## Status

**CONCLUÍDA**

## Implementado

- Rota privada `/reports/audit` e item Auditoria no menu Relatórios;
- consumo tipado do endpoint `/api/reports/audit`;
- filtros de período, contas, tipo, status, categoria e tags suportados pelo contrato;
- status ALL/ACTIVE/CANCELLED, com canceladas visíveis;
- listagem paginada e detalhes read-only;
- transferências como uma transação lógica;
- occurredAt, createdAt e updatedAt diferenciados;
- origem/destino, categoria, tags e attachmentCount sem requests por linha;
- ausência de mutações ou histórico fictício de versões;
- responsividade mobile com cards e sem overflow global;
- estados loading/error/empty/retry.

## Validação

- frontend lint: **PASS**;
- frontend build: **PASS**;
- smoke E2E geral desktop/mobile: **2 passed**;
- backend atualizado e endpoint protegido validado durante a execução manual.

## Pendência conhecida

O datepicker visual da tela de auditoria permanece pendente de correção definitiva. Os campos possuem suporte de calendário no código, mas o ícone/overlay não foi validado de forma confiável no navegador e deve ser tratado em uma correção de UI dedicada.

## Fora de escopo

Exportação, PDF/CSV/XLSX, event sourcing, histórico imutável, auditoria de segurança e TASK-5.7.

**READY FOR TASK-5.7 — Exportação dos Relatórios**
