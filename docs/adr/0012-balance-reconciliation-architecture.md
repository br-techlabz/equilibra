# ADR 0012 — Arquitetura de Reconciliação de Saldo

## Status

Aceita e aprovada para orientar a Sprint 9 (Tasks 9.1 a 9.4). Nenhum código de implementação foi executado nesta tarefa.

## Contexto

O Equilibra possui um ledger financeiro derivado onde o saldo de uma conta de ativo (`AssetAccount`) é calculado como:

```text
balance = initialBalance + somatório dos efeitos de todas as transações ACTIVE (receitas, despesas, transferências e pagamentos de fatura) vinculadas à conta.
```

Com o uso contínuo, discrepâncias entre o saldo real (banco/extrato) e o saldo calculado pelo sistema podem ocorrer devido a esquecimentos de lançamentos, taxas bancárias não registradas ou pequenos desvios. A funcionalidade de **Reconciliação de Saldo** permite que o usuário informe o saldo real em uma determinada data para ajustar o saldo calculado pelo sistema ao valor real de forma auditável e transparente.

## Decisão

1. **Abordagem baseada no Ledger (FinancialTransaction)**:
   A reconciliação não altera diretamente o `initialBalance` da conta após o primeiro lançamento, nem persiste um campo mutável de saldo atual. A correção é realizada mediante a criação automática de uma transação financeira padrão (`INCOME` ou `EXPENSE`) com a descrição exata **"Ajuste de Saldo"**.

2. **Cálculo Histórico na Data**:
   Para determinar a diferença, o sistema calcula o saldo da conta no final da data informada (`occurredAt` até o final do dia, i.e., `23:59:59.999999 UTC` ou equivalente), considerando:
   - Saldo inicial (`initialBalance`);
   - Todas as transações ativas (`ACTIVE`) com ocorrência menor ou igual à data de reconciliação selecionada.

3. **Cálculo da Diferença e Geração de Ajuste**:
   - `Diferença = Saldo Informado pelo Usuário − Saldo Calculado pelo Sistema na Data`.
   - Se `Diferença > 0`: Cria-se uma **Receita (`INCOME`)** na conta selecionada, com valor igual à `Diferença`, descrição **"Ajuste de Saldo"**.
   - Se `Diferença < 0`: Cria-se uma **Despesa (`EXPENSE`)** na conta selecionada, com valor absoluto igual a `|Diferença|`, descrição **"Ajuste de Saldo"**.
   - Se `Diferença = 0`: Nenhuma transação é gerada (retorno informativo de saldo já reconciliado).

4. **Categoria Dedicada "Ajuste de Saldo"**:
   - O sistema provisionará (de forma idempotente por usuário) uma categoria especial denominada **"Ajuste de Saldo"** com aplicabilidade `BOTH` (ou adequada para receitas/despesas de ajuste).
   - As transações geradas pela reconciliação utilizarão obrigatoriamente essa categoria.

5. **Isolamento e Segurança (Ownership-aware)**:
   - O proprietário da conta e da transação de ajuste é obtido exclusivamente do contexto autenticado (`CurrentUser.id()`).
   - Requisições informando `ownerId` ou tentando reconciliar contas de outros usuários retornam `404 Not Found`.
   - Cartões de crédito (`CreditCard`) não possuem saldo bancário direto e não são reconciliáveis por este mecanismo (reconciliação restrita a `AssetAccount` ativas).

## Modelo de Dados e Fluxo Conceitual

### Fluxo do Usuário
1. Na listagem de Contas de Ativo (`AssetAccount`), o usuário aciona a opção **"Reconciliar"** para uma conta específica.
2. O modal/tela solicita:
   - **Data da Reconciliação** (padrão: data atual; restrições: não permitir datas futuras, e avaliar limites históricos se necessário);
   - **Saldo Informado** (`BigDecimal`, monetário).
3. Ao submeter, o backend:
   - Valida a existência, atividade e ownership da conta;
   - Calcula o saldo histórico na data informada;
   - Computa a diferença;
   - Se `Diferença != 0`, obtém ou cria a categoria "Ajuste de Saldo" para o usuário;
   - Persiste a transação de ajuste (`INCOME` ou `EXPENSE`) usando os serviços existentes de transação;
   - Retorna o resultado da reconciliação (saldo calculado anterior, saldo informado, diferença e ID da transação de ajuste gerada, se houver).

### Regras Adicionais e Invariantes
- **Reconciliações Múltiplas**: Reconciliações repetidas na mesma data são permitidas. Cada execução calcula o saldo atualizado na data e gera um novo ajuste se ainda houver discrepância, sem apagar ajustes anteriores.
- **Transações Posteriores**: Lançamentos com `occurredAt` posteriores à data de reconciliação **não são alterados nem apagados**. O ajuste afeta o saldo na data e propaga-se naturalmente para os saldos subsequentes através do ledger.
- **Edição e Cancelamento**: A transação de ajuste gerada é uma transação financeira comum (`FinancialTransaction`). O usuário pode visualizá-la no histórico e cancelá-la se necessário, o que reverterá o efeito da reconciliação de forma consistente.
- **Concorrência**: Protegido pelas transações de banco (`@Transactional`) e controle de versão otimista (`@Version`) nas entidades.

## Impactos nos Módulos

- **Saldo da Conta**: Atualizado dinamicamente pelo ledger incluindo a nova transação de ajuste.
- **Histórico e Relatórios**: A transação "Ajuste de Saldo" aparece no extrato e relatórios como receita ou despesa ordinária vinculada à categoria dedicada.
- **Dashboard e Fluxo de Caixa**: Refletem o ajuste imediatamente a partir da data de ocorrência.
- **Orçamentos**: Podem opcionalmente incluir ou excluir a categoria "Ajuste de Saldo" conforme a configuração de orçamentos por categoria, devendo a arquitetura de relatórios/orçamentos respeitar a natureza neutra ou corretiva do ajuste.

## Casos Golden (Exemplos de Teste)

1. **Caso Saldo Correto (`Diferença = 0`)**:
   - Conta com saldo calculado em R$ 1.000,00. Usuário informa R$ 1.000,00.
   - Resultado: Nenhuma transação criada, resposta de sucesso informando saldo zerado de ajuste.
2. **Caso Saldo Menor (`Diferença < 0`)**:
   - Conta com saldo calculado em R$ 1.500,00. Usuário informa R$ 1.400,00 (diferença de -R$ 100,00).
   - Resultado: Cria despesa de R$ 100,00 com descrição "Ajuste de Saldo". Saldo passa a ser R$ 1.400,00.
3. **Caso Saldo Maior (`Diferença > 0`)**:
   - Conta com saldo calculado em R$ 800,00. Usuário informa R$ 950,00 (diferença de +R$ 150,00).
   - Resultado: Cria receita de R$ 150,00 com descrição "Ajuste de Saldo". Saldo passa a ser R$ 950,00.

## Plano das Tasks (Sprint 9)

- **TASK-9.1** — Backend de Cálculo de Saldo Histórico e Provisionamento de Categoria de Ajuste.
- **TASK-9.2** — Backend de Execução de Reconciliação (Criação de Transação de Ajuste) e Endpoints REST.
- **TASK-9.3** — Frontend de Reconciliação de Saldo (Modal/Tela na Listagem de Contas, formulários e integração).
- **TASK-9.4** — Testes de Integração, Casos Golden, Gate da Sprint 9 e Documentação Final.
