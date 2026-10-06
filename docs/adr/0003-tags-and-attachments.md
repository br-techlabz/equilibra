# ADR 0003 — Tags e Attachments

## Status

Aceita para orientar a Sprint 4. Esta decisão é arquitetural: não implementa CRUD de Tags, upload, download, migrations ou interfaces nesta tarefa.

## Contexto

A Sprint 3 estabilizou o ledger operacional do Equilibra com `FinancialTransaction` como entidade central, saldos derivados, cancelamento lógico, JWT stateless e isolamento por `CurrentUser`. O produto precisa enriquecer transações com marcadores organizacionais e documentos, sem introduzir uma segunda fonte de verdade financeira ou expor arquivos privados.

A auditoria do código atual não encontrou packages de Tag, Attachment, upload, multipart, storage, S3, MinIO, Blob ou recursos estáticos de arquivos. O backend usa monólito modular (`api/application/domain/infrastructure`), Spring MVC, MySQL/Flyway e Problem Details. O Docker Compose possui MySQL e backend, mas nenhum volume de arquivos. O frontend Angular ainda não possui UI para esses recursos.

## Decisões

### 1. Metadados não alteram o ledger

Tags e Attachments pertencem à transação lógica e não a movimentos individuais. Adicionar, remover ou alterar qualquer metadado nunca pode alterar:

- `amount`;
- contas ou saldo corrente;
- patrimônio líquido;
- Entradas, Saídas ou Saldo do Dashboard;
- movimentos do ledger.

Transferências recebem um único conjunto de Tags e Attachments; não existe conjunto separado para origem e destino.

### 2. Tags

Tag é um rótulo organizacional transversal, diferente de Category:

- Category é a classificação financeira principal;
- Tag é um marcador adicional, como `viagem`, `trabalho` ou `reembolsável`.

A Tag pertence a exatamente um usuário e seguirá o padrão de UUID e ownership do sistema. O modelo futuro será:

```text
Tag
- id: UUID string
- ownerId: UUID string
- name: texto apresentado
- normalizedName: nome normalizado
- active: boolean
- createdAt: Instant
- updatedAt: Instant
```

`name` é obrigatório, recebe trim, não pode ser vazio e terá limite de 100 caracteres, consistente com Category. A apresentação preserva acentos e capitalização informada; a unicidade usa comparação case-insensitive do nome normalizado, sem remover acentos. O mesmo owner não poderá ter duas Tags ativas com o mesmo `normalizedName`; owners diferentes podem usar o mesmo nome.

Tag terá ciclo de vida lógico (`active`): uma Tag inativa permanece visível em transações históricas, não pode ser associada a novos lançamentos e pode ser reativada. O fluxo normal não fará exclusão física.

A associação será many-to-many entre `FinancialTransaction` e Tag, com zero ou mais Tags por transação. A associação deve ser única por par e não aceitar Tag de outro owner. Ownership será validado na camada de aplicação por `CurrentUser` e, quando possível, reforçado por constraints/queries do banco.

Tags permanecem associadas a transações canceladas para preservar histórico. Como transações canceladas são imutáveis no domínio atual, a edição normal da transação não altera suas Tags; uma futura operação explícita de metadata deverá ser decidida em task própria.

### 3. Modelo de associação de Tags

A migration futura deverá criar uma tabela equivalente a:

```text
financial_transaction_tags
- transaction_id FK financial_transactions(id)
- tag_id FK tags(id)
- PRIMARY KEY (transaction_id, tag_id)
```

As FKs não devem permitir apagar transações do ledger por cascata perigosa. Índices futuros devem cobrir `(transaction_id)` e `(tag_id)`. Não criar essa tabela nesta TASK-4.0.

### 4. Attachments

Attachment é um arquivo privado associado a uma transação. A cardinalidade escolhida é **0..N por transação**, embora a UX inicial possa começar com um arquivo por interação. Isso evita migração estrutural quando uma nota fiscal precisar ser acompanhada por recibo, comprovante ou documento complementar.

A entidade futura será `TransactionAttachment` e armazenará metadata no MySQL:

```text
TransactionAttachment
- id: UUID string
- transactionId: UUID string
- ownerId: UUID string explícito para defesa de profundidade e queries ownership-aware
- originalFileName: nome para apresentação/download
- storageKey: identificador lógico gerado pelo servidor
- contentType: MIME validado pelo servidor
- sizeBytes: tamanho validado
- checksumSha256: integridade
- createdAt: Instant
```

`ownerId` será mantido explicitamente, apesar de também ser derivável pela transação, para facilitar isolamento, auditoria e defesa contra inconsistências. A aplicação deverá confirmar que Attachment, Transaction e owner autenticado são compatíveis.

O arquivo binário não será armazenado em BLOB no MySQL. O MySQL será fonte de verdade da metadata e da autorização; o storage será fonte dos bytes. Isso mantém o banco adequado a transações e consultas e permite migrar de filesystem para object storage sem mudar o domínio.

### 5. Storage abstraction

A camada de infraestrutura deverá expor uma porta técnica equivalente a `AttachmentStorage`/`FileStorage`, com operações conceituais:

```text
store(key, content, metadata)
open(key)
delete(key)
exists(key)
```

A implementação inicial será filesystem local configurável para DEV/MVP. O domínio e a aplicação não dependerão de `java.nio.file.Path` nem conhecerão diretórios físicos. A implementação futura poderá usar S3-compatible ou MinIO.

O `storageKey` será gerado pelo servidor, por exemplo com segmentos lógicos de owner, transaction e attachment. Nome original nunca será usado como caminho. Não haverá pasta de upload exposta como static resource público, nem path absoluto enviado ao frontend.

Docker deverá futuramente montar um volume persistente para o storage local. Em ambiente distribuído ou com container efêmero, filesystem local não é a arquitetura final; object storage será a evolução prevista.

### 6. Upload futuro

O fluxo futuro será:

1. autenticar pelo JWT;
2. carregar a transação por `id + CurrentUser.id()`;
3. validar status e política de edição;
4. validar arquivo e limites;
5. gerar `attachmentId` e `storageKey` no servidor;
6. gravar os bytes no storage;
7. persistir metadata dentro da transação de banco;
8. se o banco falhar, executar compensação removendo o arquivo;
9. retornar apenas metadata pública segura.

Se o storage falhar, nenhuma metadata será criada. Como banco e storage não possuem transação distribuída, a operação usará compensação simples no MVP e documentará o risco residual de falha durante a compensação. Um job futuro de varredura/limpeza de órfãos poderá reconciliar storage e banco; não será criado nesta tarefa.

### 7. Limites e tipos

Política inicial escolhida:

- máximo de 10 MB por arquivo;
- máximo de 10 Attachments por transação;
- arquivo vazio é rejeitado;
- whitelist: `application/pdf`, `image/jpeg`, `image/png`, `image/webp`;
- GIF não será aceito inicialmente;
- SVG, HTML, executáveis, scripts e JARs são rejeitados;
- limite multipart do Spring deverá ser igual ou superior ao limite de negócio, sem contradição.

A extensão e o `Content-Type` enviado pelo cliente não são autoridade. A implementação futura deve validar assinatura/magic bytes para PDF, JPEG, PNG e WebP, sem prometer detecção completa de malware. Malware scanning é risco/feature futura e não será simulado.

O nome original terá limite de 255 caracteres e será sanitizado para control characters, CR/LF, path traversal e injeção de headers. Unicode seguro, inclusive acentos, deve ser preservado para apresentação. `Content-Disposition` deverá usar encoding apropriado; nunca concatenar nome cru.

### 8. Download futuro

Download será sempre endpoint autenticado e owner-scoped. Contrato preferencial:

```text
GET /api/attachments/{attachmentId}/content
```

O servidor resolve o owner pela metadata e retorna 404 para Attachment inexistente ou pertencente a outro usuário. Não haverá URL pública previsível nem autorização baseada apenas em `storageKey`.

A resposta deverá controlar `Content-Type` a partir da metadata validada, usar `Content-Disposition: attachment`, `X-Content-Type-Options: nosniff` e headers que evitem cache público de conteúdo privado. Streaming deverá ser usado quando a implementação for criada, evitando carregar arquivos grandes integralmente em memória. Range requests e preview inline ficam fora do MVP.

### 9. Delete e ciclo de vida

Remover Attachment não cancela a transação. Cancelar uma transação não remove automaticamente seus Attachments; o histórico mantém os documentos. Editar amount, conta, data ou uma Transfer não apaga metadata.

A remoção futura deverá validar ownership, marcar/persistir a decisão de metadata e remover bytes com compensação adequada. Se for necessário representar estado de exclusão pendente, isso será definido na task de implementação; não criar scheduler ou transação distribuída nesta etapa.

### 10. Segurança

O fluxo de autorização será:

```text
JWT → CurrentUser → Attachment metadata → Transaction owner → storage
```

Todos os endpoints privados usarão `CurrentUser.id()` exclusivamente. O cliente nunca enviará `ownerId` como fonte de autorização. Cross-owner para Tag, associação, metadata ou download retornará 404, seguindo a política de recursos privados.

Conteúdo de arquivo não será logado. Logs podem registrar IDs técnicos e Request ID quando necessário, mas não bytes nem conteúdo sensível. Downloads não devem ser públicos ou armazenados em cache compartilhado.

### 11. APIs futuras

A arquitetura reserva, sem implementar nesta task:

```text
POST   /api/tags
GET    /api/tags
GET    /api/tags/{id}
PUT    /api/tags/{id}
PATCH  /api/tags/{id}/deactivate
PATCH  /api/tags/{id}/activate

GET    /api/transactions/{transactionId}/attachments
POST   /api/transactions/{transactionId}/attachments
GET    /api/attachments/{attachmentId}/content
DELETE /api/attachments/{attachmentId}
```

A associação de Tags poderá ser atualizada por DTOs específicos dos casos de uso de transação ou por endpoint de metadata explicitamente documentado. A decisão de contrato detalhada pertence às tasks seguintes. Filtro futuro de histórico por `tagId` é previsto, mas não será implementado na TASK-4.0.

### 12. Frontend futuro

A UI futura deverá reutilizar Angular Material e o Design System existente, sem nova biblioteca de chips/upload. Componentes conceituais:

- `TagSelectorComponent` para múltiplas Tags, seleção, remoção e eventual criação em task posterior;
- `TransactionAttachmentsComponent` reutilizável em Expense, Income e Transfer;
- indicador de Tags/Attachments no Histórico quando houver requisito.

Dashboard não exibirá Tags/Attachments nesta sprint. Não criar componentes agora.

### 13. Integridade financeira

A implementação futura deverá conter testes demonstrando que adicionar/remover Tag ou Attachment mantém exatamente os mesmos:

- saldos derivados;
- patrimônio;
- Entradas, Saídas e Saldo do Dashboard;
- amount, type, status e movements.

Transfer continua sendo uma única transação lógica com um conjunto único de metadata.

### 14. Testes obrigatórios da sprint de implementação

- Tag pertence ao owner autenticado;
- duplicidade ativa por owner é rejeitada;
- owners diferentes podem usar o mesmo nome;
- associação duplicada não é criada;
- associação cross-owner retorna 404/erro de contrato definido;
- Tag inativa não pode ser associada a novo lançamento e permanece no histórico;
- cancelamento preserva Tags e Attachments;
- Attachment de outro owner retorna 404 em metadata/download/delete;
- limite de tamanho, quantidade e arquivo vazio são rejeitados;
- PDF, JPEG, PNG e WebP válidos são aceitos;
- arquivo renomeado/executável, SVG, path traversal e CRLF são rejeitados;
- falha de storage não cria metadata;
- falha de persistência remove o arquivo gravado quando possível;
- não existe URL pública/static para uploads;
- invariantes financeiras permanecem inalteradas.

Testes de storage usarão diretório temporário isolado e limpeza garantida. Integrações de banco usarão MySQL/Testcontainers conforme o padrão do projeto.

## Consequências

A decisão mantém o ledger simples e auditável, separa metadata de finanças e evita BLOBs no banco. O custo é a necessidade de compensação entre banco e storage e de validação rigorosa de conteúdo/ownership. Filesystem local é suficiente para DEV/MVP, mas exige volume persistente e deverá ser substituído ou complementado por object storage em produção distribuída.

## Escopo explicitamente adiado

CRUD de Tags, migrations, associação em transações, multipart, upload, download, delete, storage local, S3/MinIO, scanner de malware, thumbnails, OCR, parsing de PDF, preview inline, quotas, rate limiting, signed URLs, componentes Angular e filtro por Tag no Histórico.
