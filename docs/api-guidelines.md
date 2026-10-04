# Diretrizes de API REST do Equilibra

## Objetivo

Este documento define convenções iniciais para APIs REST do Equilibra, incluindo respostas de sucesso, erros, validação, paginação e correlação de requisições.

As diretrizes devem ser aplicadas aos módulos futuros sem implementar endpoints fictícios apenas para documentação.

## Padrão REST

A API deve usar HTTP de forma semântica.

### GET

Operações de leitura não devem alterar estado.

### POST

Utilizado para criação de recursos ou operações não idempotentes.

Criação bem-sucedida deverá preferencialmente retornar:

```text
201 Created
```

e header `Location` quando aplicável.

### PUT

Quando utilizado, deverá representar substituição/atualização idempotente conforme contrato do recurso.

### PATCH

Poderá ser utilizado futuramente para atualização parcial quando houver benefício claro.

### DELETE

Exclusão bem-sucedida poderá retornar:

```text
204 No Content
```

## Respostas de sucesso

Não utilizar envelope global obrigatório como:

```json
{
  "success": true,
  "data": {}
}
```

Prefira respostas REST naturais.

Exemplos futuros:

- `GET /accounts` → coleção ou página de contas.
- `GET /accounts/{id}` → representação da conta.
- `POST /accounts` → `201 Created`.
- `PUT`/`PATCH` → `200 OK` ou `204 No Content`, conforme contrato.
- `DELETE` → `204 No Content` quando apropriado.

## Status HTTP

Status esperados inicialmente:

- `200 OK`: leitura ou operação síncrona bem-sucedida com corpo.
- `201 Created`: recurso criado.
- `204 No Content`: operação bem-sucedida sem corpo.
- `400 Bad Request`: JSON inválido, parâmetros inválidos, tipos incompatíveis, validação sintática/estrutural.
- `401 Unauthorized`: ausência de autenticação válida.
- `403 Forbidden`: usuário autenticado sem permissão suficiente.
- `404 Not Found`: recurso inexistente.
- `409 Conflict`: conflito de estado ou unicidade.
- `500 Internal Server Error`: erro inesperado.

Não retornar sempre `200 OK`.

## Problem Details

Erros devem usar Problem Details conforme RFC 9457 quando apropriado, aproveitando `ProblemDetail` do Spring.

Content-Type esperado quando negociado pelo Spring:

```text
application/problem+json
```

Não criar envelope genérico de erro como:

```json
{
  "success": false,
  "data": null,
  "error": {}
}
```

Campos nativos esperados:

- `type`
- `title`
- `status`
- `detail`
- `instance`

Propriedades adicionais permitidas quando úteis:

- `timestamp`: `Instant` em ISO-8601.
- `requestId`: identificador de correlação da requisição.
- `errors`: lista de erros de campo em validações.

## Validação

Validações com Jakarta Bean Validation e `@Valid` devem retornar `400 Bad Request` quando o request for sintaticamente/estruturalmente inválido.

Quando houver erros de campos, a resposta deverá permitir ao frontend identificar campo e mensagem:

```json
{
  "errors": [
    {
      "field": "description",
      "message": "must not be blank"
    },
    {
      "field": "amount",
      "message": "must be greater than zero"
    }
  ]
}
```

Mensagens devem ser apropriadas para consumo do frontend, mas internacionalização completa não faz parte da TASK-0.6.

## Política para 422

Não utilizar `422 Unprocessable Content` automaticamente para toda validação.

Validação sintática/estrutural de request deve normalmente retornar `400 Bad Request`.

Erros de regra de negócio poderão utilizar outro status quando houver justificativa semântica clara, mas essa decisão deverá ser feita no contexto da funcionalidade concreta.

## Request ID

Toda requisição HTTP deve possuir identificador de correlação.

Header utilizado:

```text
X-Request-ID
```

Comportamento:

1. Se o cliente enviar um `X-Request-ID` válido, ele poderá ser reutilizado.
2. Se estiver ausente ou inválido, o backend gera um novo UUID.
3. O valor final é devolvido na resposta pelo header `X-Request-ID`.
4. Respostas de erro podem incluir `requestId` no Problem Details.
5. O request ID deve ser colocado no MDC durante a requisição e removido ao final.

Valores enviados pelo cliente não devem ser aceitos cegamente. Eles devem ter formato e tamanho controlados para evitar log injection ou conteúdo arbitrário.

## Logging

Usar SLF4J e MDC quando apropriado.

Não registrar:

- senhas;
- tokens JWT;
- header `Authorization`;
- conteúdo completo de anexos;
- dados financeiros sem necessidade.

Erros inesperados podem registrar stack trace internamente.

Erros esperados de validação não precisam gerar logs em nível `ERROR`.

## Paginação

Endpoints que retornarem coleções potencialmente grandes deverão possuir paginação server-side.

Ao usar Spring Data `Page`, avalie se expor a estrutura do framework cria acoplamento indesejado no contrato público.

Uma representação estável de paginação deverá ser definida quando endpoints paginados reais forem implementados.

Não retornar listas ilimitadas de transações, auditoria, anexos ou relatórios detalhados.

## CORS

CORS ainda não é necessário nesta etapa porque o frontend Angular será configurado posteriormente.

Quando houver necessidade concreta, a configuração deverá ser segura e configurável por ambiente.

Não utilizar `*` indiscriminadamente junto com credenciais.

## Segurança

O backend é responsável por autorização e isolamento de dados.

O frontend não é barreira de segurança.

Endpoints futuros não devem confiar em `userId` informado pelo frontend para autorização ou propriedade de dados.

**Regra permanente (TASK-1.6):**
Requests de recursos privados não devem aceitar `userId` como identificador do proprietário quando ele puder ser derivado da autenticação. O proprietário deve vir exclusivamente de `CurrentUser.id()` (obtido do contexto autenticado Spring Security). Parâmetros como `?userId=...`, headers como `X-User-ID` ou campos no corpo da requisição como `{ "userId": "..." }` **não devem ser usados** para determinar ownership de recursos privados. A identidade autenticada via JWT é a única fonte confiável.

## OpenAPI

A documentação OpenAPI deve permanecer compatível com o contrato REST.

Não documentar endpoints inexistentes manualmente.
