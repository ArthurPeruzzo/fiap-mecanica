# ADR-0004 — Infraestrutura em 4 repositórios Terraform com states independentes

## Contexto

A Fase 3 exige o projeto organizado em **quatro repositórios**, cada um com CI/CD próprio e
deploy automático: Lambda, Infra Kubernetes, Infra de Banco e Aplicação. Antes (Fase 2) toda a
infraestrutura vivia num único repositório e num único state Terraform.

## Decisão

Separar em 4 repositórios GitHub, cada um com seu pipeline e seu **state Terraform próprio** no
mesmo bucket S3 (`fiap-mecanica`), em chaves distintas:

| Repositório | State key | Provisiona |
|---|---|---|
| `fiap-mecanica-infra-k8s` | `tfstate/terraform.tfstate` | VPC, EKS, node group, add-ons de cluster |
| `fiap-mecanica-infra-db` | `tfstate/db.tfstate` | RDS MySQL, subnet group, security group |
| `fiap-mecanica-lambda` | `tfstate/lambda.tfstate` | Function Lambda de autenticação |
| `fiap-mecanica` | `tfstate/app-infra.tfstate` + `tfstate/apigateway.tfstate` + `tfstate/newrelic.tfstate` | ECR, API Gateway, alertas/dashboards New Relic + deploy da app |

- Dependências entre repos são **leitura de estado** via `terraform_remote_state` (somente
  leitura): `infra-k8s ← infra-db ← app-infra/apigateway`; `lambda` é independente.
- O bucket de state **não é gerenciado por Terraform** — é criado por `aws s3api create-bucket`
  idempotente no início de cada CD (contorna uma SCP de conta Academy Lab que quebra o recurso
  `aws_s3_bucket`).
- Locking via `use_lockfile = true` (nativo do S3, sem DynamoDB).
- Ordem de deploy do zero: `infra-k8s → infra-db → lambda → fiap-mecanica`.

## Consequências

**Positivas**
- Blast radius separado: `terraform destroy` da aplicação não ameaça o cluster nem o banco.
- Ciclos de vida independentes — infra de cluster/banco muda raramente; a app muda a cada commit.
- Cada pipeline é menor e mais rápido.

**Negativas / trade-offs**
- Não há orquestração automática entre os 4 CDs; a ordem de deploy é conhecimento operacional
  (documentada nos READMEs).
- Mudança que atravessa repos (ex.: novo output consumido por outro) exige coordenar dois PRs.
- `terraform_remote_state` acopla os repos pelo formato dos outputs.

