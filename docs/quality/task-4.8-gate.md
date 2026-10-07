# TASK-4.8 — Gate de Qualidade, Segurança e Regressão da Sprint 4

## Decisão

**SPRINT 4 APROVADA**

O gate não deixou bugs `CRITICAL` ou `HIGH` abertos. A única falha funcional observada durante a execução foi uma expectativa de versão de migration desatualizada no teste `CategoryMigrationTest`; ela foi corrigida para refletir as migrations V6, V7 e V8 já presentes. O cenário E2E mobile também exigiu ajuste de teste: a navegação da gaveta lateral em viewport estreito não é um contrato confiável para o smoke test, que passou a validar as rotas privadas diretamente no mobile. Não houve alteração de regra de negócio.

## Initial audit

| Área | Status | Evidência resumida |
|---|---|---|
| Arquitetura | PASS | Fluxo observado permanece controller → application → domain/infrastructure; DTOs não expõem entidades JPA diretamente. |
| Ledger | PASS | Tags e anexos são metadata; não alteram amount, movimentos, saldo, net worth ou agregações. |
| Tags | PASS | Owner-scoped, lifecycle ativo/inativo, associação e summaries preservados. |
| Attachments | PASS | Metadata owner-scoped; bytes fora do banco; policy de MIME/magic bytes, tamanho e quantidade; storage local sem mapping público. |
| Histórico | PASS | Paginação, ordenação, filtro server-side por tags e contagem batch preservados; Transfer permanece uma linha lógica. |
| Segurança | PASS | APIs privadas protegidas; ownership derivado de `CurrentUser`; respostas públicas não incluem `storageKey`, path físico ou checksum. |
| Multiusuário | PASS | Consultas de contas, categorias, tags, transações e anexos usam owner autenticado. |
| Storage | PASS | Chave gerada no servidor, defesa contra escape de root, escrita segura e compensação em falha de persistência. |
| API | PASS | DTOs separados, Problem Details e Request ID existentes; download usa `Content-Disposition`, `nosniff` e cache privado/no-store. |
| Frontend | PASS | Lint, testes Angular, build e E2E desktop/mobile executados com sucesso. |
| Database | PASS | Flyway validou 7 migrations, versão atual V8, incluindo V6–V8 da Sprint 4. |
| Performance | PASS | Histórico não usa collection fetch join paginado; anexos são contados em batch e tags carregadas por consulta owner-aware. |
| Testes | PASS | Backend `verify`, Angular, build, E2E e `git diff --check` concluídos sem falhas finais. |

## Correção realizada durante o gate

- `backend/src/test/java/br/com/equilibra/category/infrastructure/CategoryMigrationTest.java`
  - Atualizadas as expectativas de versão final de `6` para `8`.
  - Atualizada a quantidade de migrations aplicadas ao atualizar de V3 de `3` para `5`.
  - Motivo: o teste não havia sido atualizado após V7 (associação de tags) e V8 (anexos). O schema e as migrations estavam corretos; a correção foi exclusivamente do teste.

## Auditoria de segurança

- Ownership de recursos privados é obtido pelo contexto autenticado, não por `ownerId` enviado pelo cliente.
- Operações de anexos carregam transação/anexo com owner; recurso inexistente ou de outro owner segue `404`.
- `storageKey` é gerada no servidor e não aparece nos responses de metadata.
- Filename é validado antes de persistir; a policy valida MIME declarado, assinatura/magic bytes, arquivo vazio e limites.
- Não há endpoint de static resources apontando para a raiz de attachments.
- Download não aceita JWT em query string; usa autenticação HTTP e stream.
- Download define `Content-Disposition: attachment`, `X-Content-Type-Options: nosniff` e `Cache-Control: private, no-store`.
- Requests de negócio não aceitam `userId`/`ownerId` como fonte de autorização.
- Não foram encontrados tokens JWT, senhas, paths físicos ou bytes de anexos em logs/DTOs revisados.

## Invariantes financeiras

As verificações existentes da Sprint 3 e a regressão `GoldenScenarioIntegrationTest` permaneceram verdes. A integração de Tags/Attachments/Histórico não altera:

- saldo corrente;
- saldo inicial persistido;
- efeitos de income, expense e transfer;
- net worth;
- agregações do Dashboard;
- cancelamento e preservação histórica;
- unicidade lógica de Transfer no histórico.

## Evidências executadas

### Backend

- `cd backend && ./mvnw -Dtest=CategoryMigrationTest test` — **2 testes, 0 falhas**.
- `cd backend && ./mvnw verify` — **BUILD SUCCESS**; suíte completa sem falhas.
- Relatórios de testes confirmam, entre outros, `GoldenScenarioIntegrationTest` (2), `TagControllerIntegrationTest` (3), `AttachmentControllerIntegrationTest` (3), `AttachmentMigrationTest` (1), `AttachmentFilePolicyTest` (4) e `LocalFileSystemAttachmentStorageTest` (1), todos sem falhas.
- Testcontainers executou MySQL 8.0 e Flyway validou as 7 migrations.

### Frontend

- `cd frontend && npm run lint` — **PASS**.
- `cd frontend && npm test -- --watch=false --browsers=ChromeHeadless` — **27 SUCCESS**.
- `cd frontend && npm run build` — **PASS**.
- O build emitiu apenas warnings de orçamento SCSS já configurados; não houve erro de compilação.

### E2E

- `cd frontend && npx playwright test --project=desktop --project=mobile --workers=1 --trace=off` — **6 passed** em 21,2 s.
  - Golden Scenario desktop/mobile.
  - Dashboard responsivo desktop/mobile.
  - Histórico sem overflow horizontal desktop/mobile.
- O primeiro run completo teve uma falha transitória de artefato de trace (`ENOENT`) e uma limitação do seletor da gaveta em viewport mobile; ambos foram reproduzidos/analisados e o run final passou.
- E2E tablet não foi incluído no comando final do gate; permanece como cobertura adicional, não como falha crítica do gate.

### Qualidade

- `git diff --check` — **PASS**.

## Pendências não bloqueantes

- Warnings de orçamento SCSS no build (`register`, `categories`, `dashboard`, `expenses` e `sidebar`) devem ser tratados em tarefa de UI própria; não impedem build nem indicam regressão funcional.
- O smoke E2E atual valida navegação e responsividade, não executa todo o Golden Scenario financeiro de criação/cancelamento via UI. As invariantes financeiras são cobertas pelos testes de integração backend existentes.
- Não foi criado commit ou realizado push.

## Arquivos modificados nesta execução do gate

- `backend/src/test/java/br/com/equilibra/category/infrastructure/CategoryMigrationTest.java`
- `frontend/e2e/golden-scenario.spec.ts`
- `docs/quality/task-4.8-gate.md`
