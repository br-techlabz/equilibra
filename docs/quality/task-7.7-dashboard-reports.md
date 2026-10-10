# TASK-7.7 — Dashboard e Relatórios

## Status

**CONCLUÍDA**

## Implementado

- Serviço de previsibilidade consultando projeção e indicadores autoritativos.
- Seção `Previsibilidade Financeira` no Dashboard.
- Cards de saldo projetado, previsto a pagar, previsto a receber, atrasados e próximos.
- Separação explícita entre realizado e previsto.
- Período da projeção exibido.
- Links para Fluxo de Caixa e Agenda Financeira.
- Estados independentes de carregamento e erro.
- Retry da seção de previsibilidade.
- Nenhum cálculo financeiro paralelo no frontend.
- Nenhuma alteração no ledger.
- ADR 0010 documentado.

## Validação

- Backend tests: aprovados.
- Backend package: aprovado.
- Frontend lint: aprovado.
- Frontend build: aprovado.
- E2E dedicado do Dashboard: aprovado; 1 teste passou em 12,1s contra o frontend atualizado.
- E2E da Agenda Financeira/indicadores: aprovado; 3 testes passaram.
- Requests de projeção e indicadores retornaram `200`.
- Ausência de `ownerId` confirmada.
- Relatórios históricos e exportações preservados sem mistura de previsões.

## Limitações conhecidas

- O runner Karma permanece bloqueado após conexão com ChromeHeadless e não apresenta resumo Jasmine.
- Relatórios históricos não receberam novos agregados nesta task; a previsibilidade está integrada ao Dashboard e às telas de Fluxo de Caixa/Agenda.

## Gate E2E final

- E2E Dashboard + Agenda: 4 testes aprovados em 25,6s contra o frontend atualizado na porta 4202.
- Projeção e indicadores retornaram HTTP 200.
- Separação real/previsto e ausência de `ownerId` verificadas.

## Fora do escopo confirmado

- Alterações no ledger.
- Novas transações.
- Notificações externas.
- Alterações de exports históricos.
- TASK-7.8.
