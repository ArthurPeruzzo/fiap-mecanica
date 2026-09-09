# infra/terraform/aws — alertas + dashboard New Relic (Terraform)

Módulo Terraform que gerencia **só os 6 recursos New Relic** do projeto (política de alerta,
condição NRQL, destino/canal de notificação, workflow e o dashboard de observabilidade de
negócio). State próprio: `s3://fiap-mecanica/tfstate/newrelic.tfstate`.

## Histórico

Até 2026-09-02 este diretório concentrava toda a infraestrutura AWS (VPC, EKS, RDS, ECR,
New Relic) num state só. Em 2026-09-03 o projeto foi dividido em 4 repositórios:

| Recurso | Foi para |
|---|---|
| VPC, EKS, node group, access entries | [`fiap-mecanica-infra-k8s`](https://github.com/ArthurPeruzzo/fiap-mecanica-infra-k8s) |
| RDS, subnet group, security group | [`fiap-mecanica-infra-db`](https://github.com/ArthurPeruzzo/fiap-mecanica-infra-db) |
| ECR | [`../app-infra`](../app-infra/) (`tfstate/app-infra.tfstate`) |
| New Relic (alertas + dashboard) | **aqui** |

Até 2026-09-08 as cópias congeladas dos arquivos de VPC/EKS ainda viviam neste diretório porque
ele dividia a chave de state `tfstate/terraform.tfstate` com o `fiap-mecanica-infra-k8s`.
Isso era um risco latente: o CD daquele repo roda `terraform apply -auto-approve` sem `-target`,
e os 6 recursos `newrelic_*` presentes naquele state (mas ausentes do config dele) faziam o
Terraform planejar destruí-los — e falhar por não ter o provider `newrelic` configurado.

**Resolvido** movendo este módulo para um state próprio (`tfstate/newrelic.tfstate`) e apagando
as cópias de VPC/EKS. Os dois lados agora são independentes; não há mais `-target` no CD nem
regra de "nunca rode apply sem -target".

## Arquivos

| Arquivo | Recurso |
|---|---|
| `newrelic-alerts.tf` | `newrelic_alert_policy.os_falhas`, `newrelic_nrql_alert_condition.os_erros_500`, `newrelic_notification_destination.email`, `newrelic_notification_channel.email`, `newrelic_workflow.os_falhas` — alerta de 5xx em rotas `/ordem-servico/**` |
| `newrelic-dashboards.tf` | `newrelic_one_dashboard.observabilidade_negocio` — volume diário de OS, tempo médio por fase, taxa de erro |

## CD

Job `newrelic-apply` no `.github/workflows/cd.yml` (repo `fiap-mecanica`): `terraform init` +
`terraform apply -auto-approve` neste diretório. Sem `-target` — o módulo só tem os 6 recursos
New Relic. Credenciais via secrets `NEW_RELIC_API_KEY` / `NEW_RELIC_ACCOUNT_ID`
(`TF_VAR_newrelic_api_key` / `TF_VAR_newrelic_account_id`).

## Aplicar manualmente

```bash
cd infra/terraform/aws
terraform init
terraform apply -var newrelic_api_key=<NRAK...> -var newrelic_account_id=<account_id>
```

`newrelic_api_key`/`newrelic_account_id`: gere uma User API Key em Account settings → API keys na
New Relic (o account ID aparece na mesma tela). Diferente da license key do agente/Helm
(`NEW_RELIC_LICENSE_KEY`, usada pelo `fiap-mecanica-infra-k8s` e pelo Secret Kubernetes da app).

## Variáveis (`vars.tf`)

| Variável | Default | Descrição |
|---|---|---|
| `project_name` | `fiap-mecanica` | Prefixo no nome dos recursos |
| `region_default` | `us-east-1` | Região AWS |
| `tags` | `{Name = "fiap-mecanica-terraform"}` | Tags |
| `newrelic_api_key` | — (obrigatório, sensível) | User API Key da New Relic |
| `newrelic_account_id` | — (obrigatório) | Account ID numérico da New Relic |
| `alert_email` | `arthurkohl0@gmail.com` | E-mail que recebe os alertas |

Sem outputs — este módulo não é lido por nenhum outro via `terraform_remote_state`.
