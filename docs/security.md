# Segurança do Equilibra

## Armazenamento de Senha (Password Storage)

### Algoritmo
- **bcrypt** via `BCryptPasswordEncoder` do Spring Security.
- Cost factor (work factor): 10 (padrão do Spring Security).
- Salt gerenciado automaticamente pelo algoritmo (não há coluna separada de salt).

### Implementação Spring
- Bean `PasswordEncoder` configurado em `PasswordEncoderConfig`.
- Injetado via DI no `PasswordService`.
- Nunca instanciar `new BCryptPasswordEncoder()` diretamente em services.

### Parâmetros Relevantes
| Parâmetro | Valor | Observação |
|-----------|-------|------------|
| Algoritmo | bcrypt | Identificado pelo prefixo `$2a$` |
| Cost factor | 10 | Padrão Spring Security |
| Salt | Interno | 128 bits, gerado por hash |
| Tamanho hash | ~60 chars | Armazenado em `VARCHAR(255)` |

---

## Política de Senha (Password Policy)

| Regra | Valor | Comportamento |
|-------|-------|---------------|
| Comprimento mínimo | 8 caracteres | Rejeita se menor |
| Comprimento máximo | 128 caracteres | Rejeita se maior (protege contra DoS) |
| Vazia | Não permitida | Rejeita string vazia ou nula |
| Apenas espaços | Não permitida | Rejeita strings só com whitespace |
| Trim silencioso | **NÃO** | Espaços no início/fim são preservados |
| Unicode | Preservado | Sem normalização ou remoção de caracteres |
| Complexidade artificial | Não exigida | Não obriga maiúscula, número, símbolo |

### Rationale
- Prioriza **comprimento** sobre regras de composição.
- Permite **passphrases** (frases de senha) longas e memoráveis.
- 128 caracteres permite passphrases confortáveis sem risco de buffer overflow.

---

## Regras Obrigatórias

### Nunca
- ❌ Armazenar senha em texto puro (plaintext).
- ❌ Registrar (log) senha em texto puro.
- ❌ Registrar (log) hash da senha (`passwordHash`).
- ❌ Comparar senha manualmente com `equals()` ou `==`.
- ❌ Decodificar/reverter hash.
- ❌ Implementar algoritmo criptográfico próprio (SHA-256, MD5, Base64, AES, etc.).
- ❌ Expor `passwordHash` na API (DTOs, responses, toString).

### Sempre
- ✅ Usar `PasswordEncoder` do Spring Security.
- ✅ Validar senha via `PasswordValidator` antes de codificar.
- ✅ Verificar senha exclusivamente via `PasswordEncoder.matches(raw, encoded)`.
- ✅ Usar `PasswordService` como abstração centralizada.
- ✅ Injetar `PasswordEncoder` via Spring DI.

---

## Evolução Futura (Hash Upgrade)

### Estratégia Conceitual
O sistema está preparado para migração gradual de algoritmo via **DelegatingPasswordEncoder**:

1. **Atual**: `BCryptPasswordEncoder` (prefixo implícito).
2. **Futuro**: Migrar para `DelegatingPasswordEncoder` com mapeamento:
   ```java
   Map<String, PasswordEncoder> encoders = Map.of(
       "bcrypt", new BCryptPasswordEncoder(),
       "argon2", new Argon2PasswordEncoder()
   );
   PasswordEncoder delegating = new DelegatingPasswordEncoder("bcrypt", encoders);
   ```

3. **Hashes existentes**: Continuam funcionando (prefixo `{bcrypt}` adicionado automaticamente).
4. **Novos hashes**: Usam algoritmo padrão atual (ex: `{argon2}...`).
5. **Rehash no login**: Após `matches()` bem-sucedido, verificar `upgradeEncoding(encoded)` e, se `true`, re-codificar com algoritmo atual e persistir.

### Benefícios
- Sem redefinição em massa de senhas.
- Migração transparente no próximo login do usuário.
- Suporte a múltiplos algoritmos simultâneos.

---

## Configuração

A configuração do encoder é centralizada em `PasswordEncoderConfig`. Parâmetros não são expostos via frontend nem variáveis de ambiente inseguras.

Para alterar cost factor no futuro:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // cost factor 12
}
```

---

## Endpoints Públicos Sensíveis

O endpoint `POST /api/auth/register` é público para permitir criação de conta, mas não autentica automaticamente o usuário criado.

O endpoint `POST /api/auth/login` é público para validar credenciais e emitir um access token JWT Bearer com expiração curta (padrão 15 min).

Falhas de autenticação retornam resposta genérica para evitar enumeração de contas: usuário inexistente, senha incorreta e usuário inativo resultam externamente em `401 Unauthorized` com `Invalid email or password.`.

A API permanece stateless (`SessionCreationPolicy.STATELESS`); não há `JSESSIONID`. Autenticação via `Authorization: Bearer <access-token>`.

---

## JWT (Access Token)

### Algoritmo e Chaves
- **Algoritmo**: HS256 (HMAC SHA-256) — assinatura simétrica.
- **Secret**: configurado via `EQUILIBRA_JWT_SECRET` (mínimo 32 bytes / 256 bits). Não versionado; use variável de ambiente.
- **Issuer**: `equilibra-api` (configurável via `EQUILIBRA_JWT_ISSUER`).
- **TTL**: 15 minutos (configurável via `EQUILIBRA_JWT_ACCESS_TOKEN_TTL`).

### Claims Mínimos
| Claim | Valor | Descrição |
|-------|-------|-----------|
| `iss` | `equilibra-api` | Issuer |
| `sub` | UUID do usuário | Subject (identificador imutável) |
| `iat` | timestamp | Issued At (segundos desde epoch) |
| `exp` | timestamp | Expiration (iat + TTL) |
| `jti` | UUID | ID único do token (prepara para revogação futura) |

### Claims Proibidos
NUNCA incluídos no JWT:
- `password`, `passwordHash`
- Secrets, chaves privadas
- Dados financeiros (saldos, transações, anexos)
- Informações sensíveis desnecessárias

### Validação
O Resource Server valida em cada request:
1. Estrutura JWT válida (3 segmentos Base64URL)
2. Assinatura HS256 com secret configurado
3. Issuer corresponde ao configurado
4. Expiração (`exp`) não passou
5. Claims obrigatórios presentes

Falhas → `401 Unauthorized` (Problem Details) sem detalhes criptográficos.

### SecurityContext / Principal
Após validação, o `SecurityContext` recebe `AuthenticatedPrincipal` contendo apenas `userId` (UUID). Não há consulta ao banco a cada request (stateless).

**Trade-off documentado**: usuário desativado após emissão do token continua autenticado até o token expirar (TTL curto mitiga). Não há blacklist nesta etapa.

### Endpoints Públicos
- `POST /api/auth/register`
- `POST /api/auth/login`
- Health/Actuator/OpenAPI (conforme Sprint 0)

Demais endpoints → `401` se token ausente/inválido/expirado.

### 401 vs 403
- **401 Unauthorized**: não autenticado, token inválido/ausente/expirado/adulterado.
- **403 Forbidden**: autenticado mas sem autorização para a operação (não usado nesta etapa).

### Problem Details + Request ID
Erros de segurança (401/403) retornam `application/problem+json` com `requestId` (correlação via `X-Request-ID` / MDC). Filtros na ordem: `RequestIdFilter` → Spring Security.

### OpenAPI
Security scheme `bearerAuth` (HTTP Bearer JWT) configurado globalmente; `/register` e `/login` marcados como públicos (`@SecurityRequirements`).

### Logs
JWT completo **nunca** logado. `SecurityProblemSupport` não expõe token em erros.

Requisito futuro: avaliar rate limiting para endpoints públicos sensíveis, especialmente cadastro, login e recuperação de senha. Não há Redis, CAPTCHA ou infraestrutura dedicada nesta etapa.

---

## Contexto do Usuário Autenticado e Isolamento Multiusuário (TASK-1.6)

### Fonte Confiável da Identidade

A identidade do usuário autenticado deriva exclusivamente do JWT validado pelo Spring Security:

```
JWT (sub = UUID)
    ↓
Spring Security (Resource Server)
    ↓
SecurityContext (Authentication)
    ↓
AuthenticatedPrincipal (userId)
    ↓
CurrentUser.id() → UUID
```

### CurrentUser

Interface centralizada em `shared/api/CurrentUser` com implementação `SpringSecurityCurrentUser`:
- Obtém o UUID do `AuthenticatedPrincipal` no `SecurityContext`
- Falha explicitamente se não houver autenticação válida, for anônima, ou o principal for inválido
- Não consulta banco de dados (stateless)

### Anti-spoofing

É proibido utilizar identidades alternativas enviadas pelo cliente:
- ❌ `X-User-ID`, `User-ID`, `X-Authenticated-User`
- ❌ `?userId=...`
- ❌ `{ "userId": "..." }` no body

Testes confirmam que `/me` ignora header/query maliciosos e responde com o usuário do JWT.

### Ownership e IDOR/BOLA

**Regra fundamental**: Todo recurso financeiro privado possui um proprietário derivado de `CurrentUser.id()`. O cliente não determina ownership.

**Proteção contra IDOR/BOLA**:
- Repositories privados devem usar métodos ownership-aware: `findByIdAndOwnerId`, `findAllByOwnerId`, `existsByIdAndOwnerId`
- Evitar `findById(id)` + validação tardia
- Criação: `owner = CurrentUser.id()`
- Leitura: `findAllByOwnerId(CurrentUser.id())`
- Atualização/Exclusão: buscar recurso dentro do escopo do usuário autenticado

**Convenção para recurso de outro usuário**: Preferir `404 Not Found` sobre `403 Forbidden` quando negar acesso também evita confirmar a existência do recurso. `403` pode ser usado quando a existência não for sensível.

### Categorias privadas (TASK-2.4)

`Category` mantém `ownerId` obrigatório e imutável, com FK real para `users`. Consultas por ID, listagens, verificações de nome e filtros de applicability incluem o owner. Para disponibilizar categorias a despesas/receitas, o repository exige também `active=true`; `BOTH` participa de ambos os conjuntos.

Não há endpoint de categorias nesta etapa. Na futura API, a identidade deverá vir exclusivamente de `CurrentUser.id()`, nunca do payload. Métodos genéricos herdados de `JpaRepository` não substituem consultas ownership-aware em fluxos privados.

A unicidade por owner/nome ativo é protegida pelo banco, independentemente da applicability; registros inativos são preservados, e a FK não permite exclusão em cascata do histórico com o usuário. Testes MySQL de `CategoryRepositoryTest` cobrem isolamento A/B, filtros, conflitos de nome e FK.

### Endpoint `/me`

`GET /api/users/me`:
- Requer autenticação (Bearer JWT)
- Retorna `200` com `{ "id", "email", "createdAt" }`
- Não expõe `password`, `passwordHash`, claims internos
- Consulta `UserRepository` usando `CurrentUser.id()`
- Retorna `401` se token ausente/inválido/expirado
- Retorna `404` se usuário autenticado não existir mais no banco
- Participa do mecanismo `X-Request-ID`

---

## Testes

### Cobertura Obrigatória
- **Encoder**: hash gerado, hash ≠ original, salt diferente, matches true/false.
- **Política**: mínimo, máximo, vazia, apenas espaços, passphrase, especiais, Unicode, sem trim.
- **Não transformação**: `" senha "` ≠ `"senha"` após encode/matches.
- **Segredos**: Testes usam apenas valores fictícios óbvios.

### Execução
```bash
cd backend
./mvnw test
```

Todos os testes devem passar sem Testcontainers (são unitários).