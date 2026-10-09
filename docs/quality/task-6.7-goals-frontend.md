# TASK-6.7 — Frontend de Metas Financeiras

## Status

**CONCLUÍDA — interface, lint, build e testes unitários aprovados.**

## Implementado

- Rota privada `/goals`.
- Item Metas Financeiras na sidebar.
- CRUD consumindo a API real da TASK-6.6.
- Filtros ACTIVE, COMPLETED e ARCHIVED.
- Criação/edição de metas.
- Contribuições explícitas.
- Conclusão e arquivamento.
- Progresso, restante e percentual vindos do backend.
- Valores em BRL e data objetivo.
- Cards responsivos, loading, empty e error.
- Sem ownerId ou JWT em URLs.
- Nenhuma alteração no ledger.

## Testes

```bash
cd frontend
npm run lint
npm test -- --watch=false
npm run build
```

Spec criada para o serviço:

```text
src/app/features/goals/data-access/goal-api.service.spec.ts
```

E2E criado:

```text
frontend/e2e/goals.spec.ts
```

O E2E cobre listagem, progresso, filtro por status, ausência de ownerId e overflow.

## Fora do escopo

Notificações, metas compartilhadas, alterações financeiras, novos endpoints e funcionalidades da Sprint 7.
