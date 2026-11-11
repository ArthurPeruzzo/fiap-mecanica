# Banco de Dados — Mecânica FIAP (Fases 3 e 4)

Justificativa da escolha do banco, o modelo relacional e a explicação dos relacionamentos.

## Escolha do banco: MySQL gerenciado (Amazon RDS)

### Por que um banco relacional

O domínio é **fortemente relacional e transacional**: `cliente → veículo → ordem de serviço →
(serviços, peças, insumos)`, com integridade referencial entre as pontas deste banco e regras de
unicidade (placa do veículo, CPF de usuário, um vínculo por item em cada ordem). Abrir uma ordem
de serviço executa vários `INSERT` numa única unidade de trabalho (a reserva de estoque é uma chamada ao `fiap-estoque`, com compensação) — precisa
de **transações ACID**. Não há requisito de escala horizontal massiva, schema dinâmico nem dados
semiestruturados que justificassem um banco NoSQL **neste serviço**. (O `fiap-cobranca`, que guarda um pagamento por ordem de
serviço e só acessa por chave, usa DynamoDB; ver [Bancos dos demais serviços](#bancos-dos-demais-serviços).)

### Por que MySQL

| Critério | MySQL |
|---|---|
| Serviço gerenciado barato na AWS | RDS `db.t3.micro`, dentro do free tier / conta Academy Lab |
| Integração com a stack | suporte first-class no Spring Data JPA e no Flyway; driver `mysql-connector-j` maduro |
| Precisão temporal | `datetime(6)` (microssegundos) — usado nas datas de fase da ordem de serviço |
| Operação | backups automáticos, criptografia em repouso, patching gerenciado pelo RDS |

**Alternativas consideradas:** PostgreSQL — igualmente adequado; a decisão foi por familiaridade
da equipe e menor footprint na instância pequena. SQL Server — descartado pelo modelo de
licenciamento. O código **não usa nada específico de MySQL** além de tipos SQL padrão: migrar para
PostgreSQL seria trocar dialeto do Hibernate + driver, sem mudança de schema.

### Onde roda

RDS MySQL 8.0, privado (`publicly_accessible = false`), criptografado, na VPC do cluster EKS. O
security group libera a porta `3306` **apenas** para o security group do control plane do EKS — o
banco não é acessível pela internet nem pela Lambda de autenticação. Provisionado pelo repositório
[`fiap-mecanica-infra-db`](https://github.com/ArthurPeruzzo/fiap-mecanica-infra-db). Schema versionado
por Flyway (`src/main/resources/db/migration`), aplicado no startup da aplicação.

## Diagrama Entidade-Relacionamento

```mermaid
erDiagram
    ROLES ||--o{ USERS_ROLES : ""
    USERS ||--o{ USERS_ROLES : ""
    USERS ||--o| MECANICO : "perfil"
    USERS ||--o| ATENDENTE : "perfil"
    USERS ||--o| ADMINISTRADOR : "perfil"

    CLIENTE ||--o{ VEICULO : "possui"
    CLIENTE ||--o{ ORDEM_SERVICO : "solicita"
    VEICULO ||--o{ ORDEM_SERVICO : "objeto de"
    ATENDENTE ||--o{ ORDEM_SERVICO : "abre"
    MECANICO ||--o{ ORDEM_SERVICO : "atende"

    ORDEM_SERVICO ||--o{ ORDEM_SERVICO_SERVICO : ""
    SERVICO ||--o{ ORDEM_SERVICO_SERVICO : ""
    ORDEM_SERVICO ||--o{ ORDEM_SERVICO_PECA : ""
    ORDEM_SERVICO ||--o{ ORDEM_SERVICO_INSUMO : ""
    ORDEM_SERVICO ||--o| LINK_APROVACAO_ORCAMENTO : "gera"

    ROLES {
        bigint id PK
        varchar name
    }
    USERS {
        bigint id PK
        varchar cpf UK
        varchar password
    }
    USERS_ROLES {
        bigint user_id PK, FK
        bigint role_id PK, FK
    }
    MECANICO {
        bigint id PK
        bigint user_id FK
        varchar nome
        varchar especialidade
    }
    ATENDENTE {
        bigint id PK
        bigint user_id FK
        varchar nome
        varchar turno
    }
    ADMINISTRADOR {
        bigint id PK
        bigint user_id FK
        varchar nome
    }
    CLIENTE {
        bigint id PK
        varchar nome
        varchar cpf "nullable"
        varchar cnpj "nullable"
    }
    VEICULO {
        bigint id PK
        bigint cliente_id FK
        varchar placa UK
        varchar modelo
        int ano
    }
    SERVICO {
        bigint id PK
        varchar nome
        decimal preco
    }
    ORDEM_SERVICO {
        bigint id PK
        bigint cliente_id FK
        bigint veiculo_id FK
        bigint atendente_id FK
        bigint mecanico_id FK "nullable"
        varchar status
        varchar descricao
        decimal orcamento_total "nullable"
        datetime data_criacao
        datetime data_inicio_diagnostico "nullable"
        datetime data_conclusao_diagnostico "nullable"
        datetime data_envio_orcamento "nullable"
        datetime data_aprovacao "nullable"
        datetime data_finalizacao "nullable"
        datetime data_entrega "nullable"
        datetime data_cancelamento "nullable"
    }
    ORDEM_SERVICO_SERVICO {
        bigint id PK
        bigint ordem_servico_id FK
        bigint servico_id FK
        decimal preco
        varchar status
        datetime data_inicio_execucao "nullable"
        datetime data_fim_execucao "nullable"
    }
    ORDEM_SERVICO_PECA {
        bigint id PK
        bigint ordem_servico_id FK
        bigint peca_id "id no fiap-estoque, sem FK"
        int quantidade
        decimal preco
    }
    ORDEM_SERVICO_INSUMO {
        bigint id PK
        bigint ordem_servico_id FK
        bigint insumo_id "id no fiap-estoque, sem FK"
        int quantidade
        decimal preco
    }
    LINK_APROVACAO_ORCAMENTO {
        bigint id PK
        bigint ordem_servico_id FK, UK
        char token UK
        datetime data_expiracao
        datetime data_utilizacao "nullable"
    }
    OUTBOX_EVENTO {
        char id PK
        varchar tipo
        varchar exchange
        varchar routing_key
        bigint agregado_id "id da OS, sem FK"
        varchar payload
        datetime criado_em
        datetime enviado_em "nullable"
        int tentativas
        varchar ultimo_erro "nullable"
        varchar traceparent "nullable"
    }
```

## Relacionamentos

### Segurança e perfis

- **`users` N:N `roles`** pela tabela de junção `users_roles` (PK composta `user_id + role_id`).
  Perfis: `ROLE_ATENDENTE`, `ROLE_MECANICO`, `ROLE_ADMINISTRADOR` e `ROLE_CLIENTE` (todos com seed em `V1`).
- **`mecanico` / `atendente` / `administrador` → `users`** (N:1 via `user_id NOT NULL`; na
  prática 1:1). Cada perfil funcional é um registro próprio com dados de RH (nome, turno,
  especialidade) apontando para o `users` que autentica.
- **`cliente` NÃO tem FK para `users`.** O cliente é modelado só como parte de negócio. Quando
  ele se autentica pela Lambda (por CPF), a aplicação cria sob demanda um `users` com o mesmo CPF
  e `ROLE_CLIENTE` — a ligação entre `cliente` e `users` é **pelo valor do CPF**, não por chave
  estrangeira. É uma simplificação consciente: cliente não faz login por senha, e a `users` só
  existe para o `sub` do JWT.

### Cadastro

- **`cliente` 1:N `veiculo`** (`veiculo.cliente_id NOT NULL`). `veiculo.placa` é `UNIQUE`
  (identificador natural). O vínculo veículo↔cliente é imutável após a criação.
- **`cliente`** aceita **CPF ou CNPJ**, ambos `NULL` no schema — pessoa física ou jurídica sem
  tabela separada. A regra "exatamente um dos dois" é validada no domínio (`@DocumentoValido`),
  não pelo banco.
- **Peças, insumos e reservas** não existem neste banco: pertencem ao microsserviço `fiap-estoque`,
  com banco próprio. `ordem_servico_peca.peca_id` e `ordem_servico_insumo.insumo_id` guardam só o
  id do item, **sem FK**; o preço é o *snapshot* obtido do catálogo do estoque no vínculo.

### Ordem de serviço

- **`ordem_servico` N:1** com `cliente`, `veiculo`, `atendente` (todas `NOT NULL`) e com
  `mecanico` (**`mecanico_id` NULL**). A ordem nasce sem mecânico (status `RECEBIDA`); o mecânico
  que inicia o diagnóstico assume a ordem e passa a ser o responsável.
- Os **8 timestamps de fase** (`data_inicio_diagnostico` … `data_entrega`, `data_cancelamento`)
  ficam como colunas na própria `ordem_servico`, não numa tabela de histórico de status — o ciclo
  de vida é linear e curto e cada transição ocorre no máximo uma vez. `orcamento_total` é
  calculado ao concluir o diagnóstico.
- **`ordem_servico` N:N `servico` / `peca` / `insumo`** pelas tabelas de junção
  `ordem_servico_servico`, `ordem_servico_peca`, `ordem_servico_insumo`. São junções **com
  atributos próprios**:
  - `preco` — *snapshot* do preço no momento do vínculo, para o orçamento não mudar se o catálogo
    for reajustado depois;
  - `quantidade` — em `ordem_servico_peca` / `ordem_servico_insumo`;
  - `status` (`NAO_INICIADO` / `EM_EXECUCAO` / `FINALIZADO`) + `data_inicio_execucao` /
    `data_fim_execucao` — em `ordem_servico_servico`, porque cada serviço da ordem evolui
    individualmente.
  - `UNIQUE (ordem_servico_id, <item>_id)` em cada junção impede vínculo duplicado no nível do
    banco — o domínio soma a quantidade em vez de inserir uma segunda linha.
- **`ordem_servico` 1:1 `link_aprovacao_orcamento`** (`ordem_servico_id` e `token` ambos
  `UNIQUE`). Tabela separada por ter ciclo de vida próprio: token UUID gerado ao enviar o
  orçamento, expira em 3 dias, uso único (o cliente aprova/recusa por link, sem autenticar).

### Outbox de eventos (`outbox_evento`)

Tabela da saga (`V17`, e `V19` para o `traceparent`). Cada mudança de estado da OS que precisa avisar outro serviço grava uma
linha **na mesma transação** da mudança (`agregado_id` é o id da OS, sem FK, e `payload` é o corpo JSON da mensagem). O relay lê
as linhas pendentes (`enviado_em IS NULL`), publica no `exchange`/`routing_key` da própria linha, espera a confirmação do broker
e só então marca `enviado_em`; falhas incrementam `tentativas` e gravam `ultimo_erro`. O `traceparent` guarda o contexto de trace
da requisição para o consumo no outro serviço aparecer no mesmo trace. Não participa do modelo de negócio e não tem relação com
as demais tabelas.

## Bancos dos demais serviços

Cada serviço é dono do seu banco; nenhum acessa o de outro (os vínculos entre eles são ids sem FK).

| Serviço | Banco | Por quê | Onde |
|---|---|---|---|
| `fiap-mecanica` | MySQL 8.0 (RDS `fiap-mecanica-db`) | Modelo relacional com integridade referencial e transações (esta página) | `fiap-mecanica-infra-db` |
| `fiap-estoque` | MySQL 8.4 (RDS próprio `fiap-estoque-db`, banco `estoque`) | Saldo e reservas precisam de transação atômica em lote (`peca`, `insumo`, `reserva_estoque`) | `fiap-estoque/infra/terraform/app-infra` |
| `fiap-cobranca` | DynamoDB (tabela `cobranca-pagamento`, chave `ordemServicoId`) | Um pagamento por OS, acesso só por chave; o put condicional dá idempotência e o update condicional dá decisão atômica | `fiap-cobranca/infra/terraform/app-infra` (ADR-0001 de lá) |
