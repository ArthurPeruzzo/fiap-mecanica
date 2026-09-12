# RFC-0002 — Escolha do banco de dados: MySQL (Amazon RDS)

- **Relacionado:** [`banco-de-dados.md`](../banco-de-dados.md) (modelo relacional, ER e ajustes)

## Resumo

O banco é **MySQL 8.0 gerenciado (Amazon RDS)**. O domínio é fortemente relacional e
transacional, então a categoria é um RDBMS; entre os RDBMS, MySQL foi escolhido por ser o mais
barato de operar como serviço gerenciado na conta disponível, ter suporte first-class na stack
(Spring Data JPA + Flyway) e não exigir nenhum recurso proprietário.

## Contexto e motivação

Entidades: `cliente → veiculo → ordem_servico → (servico | peca | insumo)` mais segurança
(`users`, `roles`). Características:

- Integridade referencial entre todas as pontas (chaves estrangeiras).
- Regras de unicidade (placa do veículo, CPF de usuário, um vínculo por item em cada ordem).
- Operações que precisam ser atômicas — abrir uma OS faz vários `INSERT` mais baixa de estoque.
- Consultas relacionais (ordens de um cliente com nomes de veículo/serviços; tempo médio por
  fase).
- Volume baixo; sem dado semiestruturado nem necessidade de escala horizontal de escrita.

## Opções consideradas

### MySQL 8.0 (recomendada)

**Prós**
- RDS `db.t3.micro` cabe no free tier / conta Academy Lab.
- Suporte maduro em Spring Data JPA, Hibernate e Flyway; driver `mysql-connector-j` estável.
- `datetime(6)` (precisão de microssegundos), usado nas datas de fase da OS.
- RDS entrega backup automático, criptografia em repouso e patching sem esforço.

**Contras**
- Recursos analíticos/JSON menos ricos que os do PostgreSQL (irrelevante para este domínio).

### PostgreSQL

**Prós:** igualmente adequado; tipos e constraints mais ricos; muito bem suportado pela stack.
**Contras:** nenhum impeditivo — a decisão contra foi por familiaridade da equipe e por já haver
um MySQL em pé desde a Fase 2; migrar seria trocar dialeto do Hibernate + driver, sem ganho.

### SQL Server

**Contras:** modelo de licenciamento; instância gerenciada mais cara; sem vantagem para o
domínio.

### DynamoDB (NoSQL)

**Contras:** o modelo é relacional com muitos relacionamentos e consultas por junção; modelar
isso em tabela única com índices secundários seria forçado, perderia integridade referencial
declarativa e transações multi-entidade simples. Não se ganha nada em escala que o problema
exija.

## Recomendação

**MySQL 8.0 no Amazon RDS.** Encaixe natural no domínio relacional/transacional, menor custo e
atrito operacional entre as opções, e zero acoplamento proprietário — trocar para PostgreSQL
depois é barato se necessário.

## Trade-offs e riscos

- **Single-AZ, sem réplica de leitura**: sem alta disponibilidade — decisão de custo para o
  ambiente Lab, não recomendação de produção.
- **`skip_final_snapshot = true`**: um `terraform destroy` apaga os dados sem snapshot —
  coerente com a efemeridade da conta ([RFC-0001](0001-escolha-da-nuvem.md)), mas seria perigoso
  em produção.
- **Senha master via variável Terraform**: um `apply` com valor errado rotaciona a senha do
  banco; mitigado documentando que o valor precisa bater com o `TF_VAR_DB_PASSWORD` do
  repositório da aplicação.
- **`ddl-auto` desligado**: schema é só Flyway; toda mudança de modelo é uma migration nova.

## Consequências

- Provisionamento no repositório `fiap-mecanica-infra-db` (instância + subnet group + security
  group que libera `3306` só para o SG do cluster).
- Schema versionado em `src/main/resources/db/migration` (`V1`–`V17`), aplicado no startup.
- O modelo relacional, o diagrama ER e os ajustes feitos (junções com atributos, `datetime(6)`,
  `link_aprovacao_orcamento` 1:1, etc.) estão em [`banco-de-dados.md`](../banco-de-dados.md).
