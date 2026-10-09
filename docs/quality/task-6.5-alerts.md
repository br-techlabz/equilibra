# TASK-6.5 — Alertas de Limites de Gastos

## Status

**CONCLUÍDA — alertas derivados do resumo real, lint/build e testes frontend aprovados.**

## Implementação

- Estados derivados do backend: `ON_TRACK`, `WARNING`, `EXCEEDED`.
- Contagem de categorias próximas do limite e excedidas.
- Mensagens textuais e ícones acessíveis por categoria.
- Percentual acima de 100% preservado.
- Orçamento zero mostrado como `Não aplicável`.
- Barras limitadas visualmente ao contêiner sem truncar o texto.
- Competência reutilizada da tela de orçamentos.
- Nenhuma persistência, polling ou notificação externa.

## Testes

```bash
cd frontend
npm run lint
npm test -- --watch=false
npm run build
```

Resultados: lint aprovado, 29 testes aprovados e build aprovado.

E2E criado:

```text
frontend/e2e/budget-alerts.spec.ts
```

O teste usa resposta controlada do summary, valida `EXCEEDED`, percentual 115%, contagem e ausência de `ownerId`. Executar com backend/frontend ativos quando necessário.

## Fora do escopo

Não foram criados endpoints, migrations, alertas persistidos, e-mail, push, WebSocket, scheduler, metas ou alterações no ledger.
