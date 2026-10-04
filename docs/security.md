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

Requisito futuro: avaliar rate limiting para endpoints públicos sensíveis, especialmente cadastro, login e recuperação de senha. Não há Redis, CAPTCHA ou infraestrutura dedicada nesta etapa.

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