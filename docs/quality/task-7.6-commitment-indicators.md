# TASK-7.6 — Indicadores de Vencimentos e Compromissos Atrasados

## Status

**CONCLUÍDA**

## Implementado

- ADR 0009 com regras de classificação.
- Endpoint read-only owner-scoped `GET /api/commitments/indicators`.
- Classificação exclusiva de atrasados, hoje, próximos 7 dias e futuros.
- Separação de despesas e receitas.
- Contagens e valores derivados com BigDecimal no backend.
- Exclusão de compromissos efetivados e cancelados.
- Data de referência civil explícita.
- Integração dos indicadores à Agenda Financeira.
- Cards responsivos de atenção e valores.
- Atualização após cancelamento/efetivação.
- Sem alteração do ledger.

## Validação

- Backend tests: aprovados.
- Backend package: aprovado.
- Frontend lint: aprovado.
- Frontend build: aprovado.
- E2E Agenda Financeira/indicadores: 3 testes aprovados em 15,6s.
- Requests owner-scoped sem `ownerId`.
- Backend atualizado com migration/schema vigente.

## Limitações conhecidas

- O runner Karma conecta ao ChromeHeadless, mas não apresenta resumo nem encerra; esse bloqueio de infraestrutura foi documentado e não impede a validação E2E aprovada.
- A API não fornece nomes enriquecidos de conta/categoria no indicador; a agenda mantém os IDs/fallbacks existentes.

## Fora do escopo confirmado

- Notificações externas.
- Scheduler novo.
- WebSocket/polling.
- Alterações no ledger.
- Novas transações.
- TASK-7.7.
