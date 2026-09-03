# app-infra — ECR (Terraform)

Cria o repositório ECR da imagem Docker da aplicação, e expõe (via `terraform_remote_state`) os
valores que outros módulos deste repositório precisam mas não gerenciam: `db_endpoint` (repositório
`fiap-mecanica-infra-db`) e `eks_cluster_name` (repositório `fiap-mecanica-infra-k8s`).

Nasceu em 2026-09-03, na divisão do projeto em 4 repositórios. Substitui a parte de ECR que antes
vivia em [`../aws`](../aws/README.md), hoje legado (só New Relic).

## Recursos criados

| Arquivo | Recurso | O que é |
|---|---|---|
| `ecr.tf` | `aws_ecr_repository.app` | Repositório privado `fiap-mecanica` pra imagem Docker da app |
| `data.tf` | `data.terraform_remote_state.db` | Leitura somente-leitura do state de `fiap-mecanica-infra-db` |
| `data.tf` | `data.terraform_remote_state.k8s` | Leitura somente-leitura do state de `fiap-mecanica-infra-k8s` |

## Por que ECR e API Gateway ficam no repositório da aplicação

O enunciado da Fase 3 nomeia 4 repositórios (Lambda, Infra Kubernetes, Infra de Banco,
Aplicação) sem especificar onde ECR e API Gateway (também exigidos) devem morar. A decisão foi
mantê-los aqui: o [`../apigateway`](../apigateway/README.md) já descobre o hostname do ELB e
checa a existência da Function Lambda sozinho, via AWS CLI no próprio CD deste repositório —
movê-lo para o repositório de infra K8s criaria uma dependência **reversa** (aquele repositório
precisaria saber de algo que só existe depois do deploy da aplicação), resolvida só com
`repository_dispatch` entre repositórios. ECR segue o mesmo raciocínio: é o registro **desta**
imagem, ciclo de vida da aplicação, não da plataforma.

## Por que New Relic ainda não está aqui

`providers.tf` já deixa um comentário reservando o lugar. A migração dos 6 recursos
`newrelic_*` (alertas + dashboard) de `../aws` para cá foi adiada de propósito — ver o README
daquele diretório para o motivo e o passo a passo de quando migrar.

## Migração do ECR (2026-09-03) — via `import`, sem interromper nada

Diferente de recriar do zero, o ECR foi migrado com `terraform import aws_ecr_repository.app
fiap-mecanica` — mesmo esforço de um `destroy`+`create` (o ID de import é só o nome do
repositório), mas preserva as imagens já publicadas e não interrompe nada (pods já rodando não
re-puxam a imagem; só um novo deploy durante a janela de migração seria afetado, e não houve
deploy nessa janela).

## Variáveis (`vars.tf`)

| Variável | Default | Descrição |
|---|---|---|
| `project_name` | `fiap-mecanica` | Nome do repositório ECR |
| `region_default` | `us-east-1` | Região AWS |
| `tags` | `{Name = "fiap-mecanica-terraform"}` | Tags aplicadas aos recursos |

## Outputs (`output.tf`)

| Output | Uso |
|---|---|
| `ecr_repository_url` | Consumido pelo `cd.yml` (jobs `docker-build-push`, `k8s-deploy`) |
| `db_endpoint` | Passthrough de `fiap-mecanica-infra-db` — monta o `DB_URL` do ConfigMap da aplicação |
| `eks_cluster_name` | Passthrough de `fiap-mecanica-infra-k8s` — reservado para uso futuro (hoje o `cd.yml` ainda usa a env `EKS_CLUSTER_NAME` fixa, que nunca muda) |

## CI/CD

- `ci.yml` (Pull Request, matrix `[app-infra, apigateway]`): `terraform plan`.
- `cd.yml` (push na `main`, job `app-infra-apply`): `terraform apply`, consumido por
  `docker-build-push`, `k8s-deploy` e `apigw-apply`.

Nenhum secret novo — só as 3 credenciais AWS já usadas pelo resto do pipeline.

## Ordem de deploy numa infra do zero

Este módulo precisa que `fiap-mecanica-infra-k8s` **e** `fiap-mecanica-infra-db` já tenham sido
aplicados (ambos os `data.terraform_remote_state` são avaliados em tempo de `plan`, não só de
`apply` — se qualquer uma das duas chaves não existir no S3, o plan falha). Ver o README de
`fiap-mecanica-infra-k8s` para a ordem completa dos 4 repositórios.
