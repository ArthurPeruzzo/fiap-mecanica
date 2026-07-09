# Infraestrutura AWS — fiap-mecanica (Terraform)

Provisiona, na conta AWS Academy Lab, tudo que o cluster Kubernetes (`/k8s`) precisa pra rodar a aplicação: VPC, cluster EKS, banco RDS MySQL e repositório ECR pra imagem Docker.

## Recursos criados

| Arquivo | Recurso | O que é |
|---|---|---|
| `vpc.tf` | `aws_vpc.vpc_fiap` | VPC dedicada (`10.0.0.0/16`), com DNS support/hostnames habilitado (exigido pelo EKS) |
| `subnet.tf` | `aws_subnet.subnet_public` (x3) | 3 subnets públicas, uma por AZ (`us-east-1a/b/c`) — EKS exige subnets em pelo menos 2 AZs |
| `internet-g.tf` | `aws_internet_gateway.igw` | Gateway de internet da VPC |
| `route-t.tf` | `aws_route_table.rt_public` + associations | Rota `0.0.0.0/0` → IGW, associada às 3 subnets públicas |
| `iam-role.tf` | `data.aws_iam_role.lab_role` | Referência à role `LabRole`, já existente na conta AWS Academy Lab (a Lab **não permite criar IAM roles próprias** — por isso é `data`, não `resource`) |
| `eks-cluster.tf` | `aws_eks_cluster.cluster` | Cluster EKS `eks-fiap-mecanica`, `authentication_mode = "API"`, control plane usando `LabRole` como service role |
| `eks-node.tf` | `aws_eks_node_group.node-group` | Node group gerenciado (`t3.medium`, 1–3 nodes, desired 2), node role também `LabRole` |
| `access-entry.tf` | `aws_eks_access_entry.voclabs` + policy association | Dá acesso de admin do cluster ao seu usuário (`voclabs`, a role assumida pela AWS Academy Lab) |
| `access-entry.tf` | `aws_eks_access_entry.node_role` | Access entry tipo `EC2_LINUX` pra `LabRole` — necessária pros nodes conseguirem se registrar como objetos `Node` (ver "Gotchas" abaixo) |
| `database.tf` | `aws_db_subnet_group`, `aws_security_group.db_sg`, `aws_db_instance.mysql` | RDS MySQL 8.0 privado (`publicly_accessible = false`), criptografado, SG só libera porta 3306 pro SG do cluster EKS |
| `ecr.tf` | `aws_ecr_repository.app` | Repositório privado `fiap-mecanica` pra imagem Docker da app |
| `bucket.tf` | `aws_s3_bucket.bucket-backend` | Bucket S3 usado como backend remoto do state (ver bootstrap abaixo) |
| `backend.tf` | — | Configura o backend `s3` (bucket `fiap-mecanica`, `tfstate/terraform.tfstate`, `us-east-1`) |
| `providers.tf` | — | Provider `aws`, região `us-east-1` |
| `vars.tf` | — | Variáveis (ver tabela abaixo) |
| `output.tf` | — | Outputs (ver tabela abaixo) |

## Pré-requisitos

- Conta AWS Academy Lab ativa, com credenciais de sessão exportadas (`aws configure` ou variáveis `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`/`AWS_SESSION_TOKEN`) — **essas credenciais expiram por sessão**, renovar quando necessário.
- Terraform ≥ 1.5 (usa `import` blocks em versões anteriores do projeto; a versão testada foi 1.15.7).
- `aws` CLI configurado com a mesma conta.

## Bootstrap (só na primeira vez, conta/state do zero)

O bucket S3 usado como backend remoto (`bucket.tf`) é gerenciado pela **mesma** configuração Terraform que o usa como backend (`backend.tf`) — isso é um problema do tipo "ovo e galinha": `terraform init` exige que o bucket já exista, mas o bucket é criado por essa própria configuração.

Se o bucket `fiap-mecanica` ainda não existir na conta:
1. Comente temporariamente o bloco `backend "s3" { ... }` em `backend.tf`.
2. Rode `terraform init` (usa state local) e `terraform apply -target=aws_s3_bucket.bucket-backend` pra criar só o bucket.
3. Descomente o bloco `backend "s3"`.
4. Rode `terraform init` de novo — o Terraform detecta o backend novo e oferece migrar o state local pra ele (responda `yes`).
5. Siga o fluxo normal abaixo.

Na conta AWS Academy Lab atual usada neste projeto o bucket já existe e o state já está migrado — esse bootstrap só é necessário se alguém for provisionar isso do zero numa conta nova.

## Como aplicar (fluxo normal)

```bash
cd infra/terraform/aws
terraform init
terraform plan -var db_username=<usuario> -var db_password=<senha>
terraform apply -var db_username=<usuario> -var db_password=<senha>
```

`db_username`/`db_password` são obrigatórios e não têm valor default (de propósito — não ficam hardcoded no repo). São as credenciais do usuário master do RDS.

Depois do `apply`, siga para `/k8s` (ver `k8s/README.md`) pra publicar a imagem no ECR criado aqui e aplicar os manifests da aplicação no cluster.

## Variáveis (`vars.tf`)

| Variável | Default | Descrição |
|---|---|---|
| `project_name` | `fiap-mecanica` | Prefixo usado no nome da maioria dos recursos |
| `region_default` | `us-east-1` | Região AWS |
| `cidr_vpc` | `10.0.0.0/16` | CIDR da VPC |
| `tags` | `{Name = "fiap-mecanica-terraform"}` | Tags aplicadas aos recursos |
| `instance_type` | `t3.medium` | Tipo de instância dos nodes EKS |
| `db_name` | `mecanica` | Nome do banco criado no RDS |
| `db_instance_class` | `db.t3.micro` | Classe da instância RDS |
| `db_username` | — (obrigatório) | Usuário master do RDS |
| `db_password` | — (obrigatório, sensível) | Senha master do RDS |

## Outputs (`output.tf`)

| Output | Uso |
|---|---|
| `vpc_id`, `vpc_cidr`, `subnet_id`, `subnet_cidr` | Referência de rede |
| `eks_cluster_name` | Usado em `aws eks update-kubeconfig --name <valor> --region us-east-1` |
| `eks_cluster_endpoint`, `eks_cluster_certificate_authority_data` | Endpoint/CA do control plane |
| `eks_node_group_name` | Nome do node group |
| `db_endpoint` | Host:porta do RDS — vai direto no `DB_URL` do `k8s/configmap.yaml` |
| `db_name` | Nome do banco (`mecanica`) |
| `ecr_repository_url` | URI do ECR — usado no `docker build`/`push` e no `image:` do `k8s/deployment.yaml` |

## Gotchas já resolvidos (documentados aqui pra não se repetirem)

- **Access entry do node role**: `LabRole` é reusada tanto como service role do cluster quanto como node role do node group (a Lab não permite criar roles customizadas). Numa recriação do zero, a EKS pode auto-criar uma access entry `STANDARD` sem grupos pra essa identidade — isso autentica os nodes no cluster mas não autoriza nada via RBAC, e eles nunca conseguem virar objetos `Node` (`kubectl get nodes` fica vazio mesmo com o node group `ACTIVE`). `access-entry.tf` já declara a entry correta (`type = "EC2_LINUX"`, que vincula os grupos certos automaticamente) com `depends_on` no node group, evitando o bug em qualquer `apply` futuro.
- **`apply_immediately = true`** no `aws_db_instance.mysql`: sem isso, trocar a senha do RDS via `-var db_password=...` só valeria na próxima janela de manutenção (sexta de madrugada) em vez de imediatamente.
- **`force_delete = true`** no ECR e **`skip_final_snapshot = true` / `deletion_protection = false`** no RDS: facilitam `terraform destroy` sem travar em confirmações — aceitável aqui porque é ambiente de estudo, não produção.

## Simplificações conscientes (nível de estudo, não são falhas)

- Sem `required_providers` com versão fixada — não crítico pra um projeto de curso de uma pessoa só.
- Sem state locking (ex: DynamoDB) — risco de conflito de state só existe com múltiplos aplicadores simultâneos, não é o caso aqui.
- Secrets do Kubernetes (`k8s/secret.yaml`) usam só o base64 nativo do K8s, sem criptografia KMS em repouso no `etcd` do EKS — daria pra habilitar via `encryption_config` no `aws_eks_cluster`, mas não foi feito por simplicidade.

## Destruir / pausar custos

Ver `k8s/README.md` pra opções de pausa parcial (zerar node group, parar RDS). Pra zerar custo por completo:
```bash
terraform destroy -var db_username=<usuario> -var db_password=<senha>
```
Isso apaga o RDS (sem snapshot — dados são perdidos, o Flyway reseeda tudo na próxima subida) e a imagem do ECR (`force_delete = true`). Recriar depois é só repetir o `apply`, rebuild+push da imagem e reaplicar `/k8s`.
