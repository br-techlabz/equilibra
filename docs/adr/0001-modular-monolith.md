# ADR 0001 — Monólito modular organizado por domínio

## Status

Aceita.

## Contexto

O Equilibra é uma aplicação web multiusuário para gerenciamento de finanças domésticas.

O sistema possui múltiplos domínios relacionados, como autenticação, usuários, contas, categorias, tags, transações, transferências, anexos, dashboard, relatórios e auditoria.

Neste momento, não existe necessidade concreta de distribuição independente desses domínios em serviços separados. A prioridade é permitir desenvolvimento incremental, preservar fronteiras de domínio e manter baixa complexidade operacional.

## Decisão

Utilizar um **monólito modular organizado por domínio/feature**.

O backend rodará como uma única aplicação Spring Boot, com módulos internos organizados por domínio. Cada módulo deverá preservar suas responsabilidades e expor apenas o necessário para colaboração com outros módulos.

A organização interna preferencial de um módulo, quando fizer sentido, será:

```text
<domain>/
├── api/
├── application/
├── domain/
└── infrastructure/
```

Essa estrutura não deve ser aplicada mecanicamente quando não trouxer benefício.

## Consequências positivas

- Menor complexidade operacional.
- Deploy simplificado.
- Transações locais mais simples.
- Desenvolvimento incremental.
- Fronteiras de domínio preservadas dentro do mesmo processo.
- Menor custo inicial de infraestrutura e observabilidade.

## Trade-offs

- Todos os módulos compartilham o mesmo processo de aplicação.
- Falhas ou consumo excessivo de recursos em um módulo podem afetar a aplicação inteira.
- Disciplina arquitetural é necessária para preservar fronteiras entre módulos.
- A evolução para outra topologia exigirá análise futura e novas decisões arquiteturais.

## Decisões relacionadas

- Não adotar microserviços como objetivo obrigatório futuro.
- Não introduzir Kafka, RabbitMQ, Redis, Elasticsearch, Kubernetes, Spring Cloud, CQRS, Event Sourcing, GraphQL ou WebSockets sem requisito concreto.
- Documentar decisões arquiteturais relevantes futuras em ADRs.
