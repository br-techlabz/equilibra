# TASK-8.2 — Frontend de Cartões de Crédito

## Status

**IMPLEMENTADA — lint e build aprovados.**

## Implementado

- Feature lazy-loaded de cartões de crédito.
- Rota autenticada `/credit-cards`.
- Item “Cartões de Crédito” na sidebar recolhível.
- Serviço HTTP real `CreditCardsApiService` consumindo:
  - `GET /credit-cards`;
  - `POST /credit-cards`;
  - `PUT /credit-cards/{id}`;
  - `PATCH /credit-cards/{id}/deactivate`;
  - `PATCH /credit-cards/{id}/activate`.
- Modelo TypeScript alinhado à API da TASK-8.1.
- Página “Meus Cartões” com cards responsivos.
- Exibição de:
  - nome;
  - instituição;
  - apelido;
  - limite total;
  - limite disponível;
  - limite comprometido;
  - fechamento;
  - vencimento;
  - status.
- Formulário reativo de cadastro e edição.
- Validações de nome, instituição, limite, fechamento, vencimento e timezone.
- Desativação e reativação com confirmação para desativação.
- Estados loading, vazio, erro, retry e feedback via snackbar.
- Tratamento básico de conflitos `409`, `403` e `404`.
- Nenhum `ownerId` enviado.
- Nenhum PAN, CVV, fatura, compra, parcela ou pagamento simulado.
- Valores formatados em BRL `pt-BR`.
- Layout responsivo para mobile, tablet e desktop usando tokens do Design System.

## Arquivos criados

```text
frontend/src/app/features/credit-cards/models/credit-card.models.ts
frontend/src/app/features/credit-cards/data-access/credit-cards-api.service.ts
frontend/src/app/features/credit-cards/pages/credit-cards.page.ts
frontend/src/app/features/credit-cards/pages/credit-cards.page.scss
```

## Arquivos modificados

```text
frontend/src/app/app.routes.ts
frontend/src/app/layout/sidebar/sidebar.component.ts
```

## Validações executadas

### Lint

```bash
cd frontend
npm run lint
```

Resultado: aprovado; todos os arquivos passaram no lint.

### Testes unitários

```bash
npm test -- --watch=false
```

Resultado: processo concluído com ChromeHeadless conectado e sem falha reportada pelo comando.

### Build de produção

```bash
npm run build
```

Resultado: build aprovado.

Foram emitidos warnings preexistentes de orçamento de estilos, incluindo páginas existentes. A nova folha de cartões excede o budget de estilo em aproximadamente 371 bytes; não impede o build.

### E2E

O script Playwright está configurado no frontend. Não foi executada a suíte E2E completa nesta etapa porque o comando de ajuda foi usado apenas para confirmar a configuração disponível.

## Golden scenario

O formulário permite cadastrar:

```text
Cartão Principal
limite: R$ 5.000,00
fechamento: dia 3
vencimento: dia 10
```

A resposta real da API é renderizada com limite disponível de `R$ 5.000,00` e comprometido de `R$ 0,00`. A tela não chama APIs de contas ou transações, portanto não altera saldo bancário.

## Divergências e pendências

- Testes específicos de serviço e página para cartões ainda não foram adicionados; a validação executada utilizou a suíte existente, lint e build.
- A proteção contra respostas obsoletas em carregamentos rápidos ainda depende do fluxo simples de carregamento do componente; a implementação futura pode migrar a listagem para `switchMap`/cancelamento explícito.
- O build registra warning de budget para `credit-cards.page.scss`, sem falhar.
- E2E específico de cartões permanece recomendado para o gate final.

## Fora do escopo respeitado

- compras;
- parcelas;
- faturas;
- pagamentos;
- cálculo local de consumo de limite;
- dados sensíveis de cartão.

**READY FOR TASK-8.3**, com as pendências de testes específicos e E2E registradas para acompanhamento.
