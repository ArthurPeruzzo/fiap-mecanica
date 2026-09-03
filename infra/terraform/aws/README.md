# infra/terraform/aws — legado, só New Relic (Terraform)

> ⚠️ **Este diretório é legado.** Até 2026-09-02 concentrava toda a infraestrutura AWS do
> projeto (VPC, EKS, RDS, ECR, alertas/dashboards New Relic) num state só. Em 2026-09-03 o
> projeto foi dividido em 4 repositórios (exigência da Fase 3) e a maior parte destes recursos
> migrou:
>
> | Recurso | Foi para |
> |---|---|
> | VPC, EKS, node group, access entries | repositório [`fiap-mecanica-infra-k8s`](https://github.com/ArthurPeruzzo/fiap-mecanica-infra-k8s) |
> | RDS, subnet group, security group | repositório [`fiap-mecanica-infra-db`](https://github.com/ArthurPeruzzo/fiap-mecanica-infra-db) |
> | ECR | [`../app-infra`](../app-infra/) (state próprio, `tfstate/app-infra.tfstate`) |
> | **New Relic (alertas + dashboard)** | **ainda aqui — migração adiada de propósito** |

## Por que os arquivos de VPC/EKS ainda estão neste diretório

`vpc.tf`, `subnet.tf`, `eks-cluster.tf`, `eks-node.tf`, `access-entry.tf`, `iam-role.tf`,
`internet-g.tf`, `route-t.tf`, `bucket.tf` **continuam aqui, intocados** — código idêntico ao do
repositório `fiap-mecanica-infra-k8s`, que agora é quem de fato gerencia esses recursos.

Não foram apagados porque, como este diretório aponta para a **mesma chave de backend**
(`tfstate/terraform.tfstate`) que o repositório novo, apagar essas configurações sem também
fazer `terraform state rm` teria criado um risco real: o CD deste repositório roda
`terraform apply -auto-approve`, **sem revisão humana** — qualquer descompasso entre config e
state (config sem o recurso, state com o recurso) faz o Terraform planejar destruir, e o
`-auto-approve` executaria isso sozinho no próximo push.

A correção aplicada foi no `cd.yml`: o job `newrelic-legacy-apply` roda `terraform apply` com
`-target` restrito **explicitamente** aos 6 recursos `newrelic_*` — nunca aos de VPC/EKS, mesmo
que esses arquivos aqui um dia divirjam do que o repositório `fiap-mecanica-infra-k8s` aplicar.
Isso torna estruturalmente impossível este diretório destruir o cluster por acidente, não é
"hoje está tudo igual, então tá seguro" — é uma garantia que se mantém mesmo se divergir.

**Nunca rode `terraform apply` neste diretório sem `-target` explícito.** Os arquivos de
VPC/EKS/RDS/ECR aqui são cópias congeladas, mantidas só para o state permanecer consistente
com o config restante (New Relic) — remover essas cópias é um cleanup futuro possível, não
urgente.

## O que ainda é gerenciado daqui

| Arquivo | Recurso | O que é |
|---|---|---|
| `newrelic-alerts.tf` | `newrelic_alert_policy.os_falhas`, `newrelic_nrql_alert_condition.os_erros_500`, `newrelic_notification_destination.email`, `newrelic_notification_channel.email`, `newrelic_workflow.os_falhas` | Alerta de falha no processamento de OS: dispara quando `http.server.requests` reporta 5xx em rotas `/ordem-servico/**` |
| `newrelic-dashboards.tf` | `newrelic_one_dashboard.observabilidade_negocio` | Dashboard de negócio: volume diário de OS, tempo médio por fase, taxa de erro |

## Migração adiada — por quê

Diferente de RDS/ECR (migrados via `import`, sem tocar no recurso real), os objetos New Relic
seriam migrados via `destroy` + `create`, porque o `newrelic_workflow` **exige nome único** — já
provou isso numa sessão anterior deste projeto (`DUPLICATE: The name provided already exists`).
Recriar não afeta a aplicação em produção (observabilidade fica fora do caminho de requisição),
mas ainda assim é uma operação real de destruição, e ficou combinado adiar para quando fizer
sentido revisitar.

Quando migrar: copiar `newrelic-alerts.tf`/`newrelic-dashboards.tf` para `../app-infra/`
(adicionando o provider `newrelic` no `providers.tf` de lá, hoje comentado como pendência), criar
os novos objetos por lá, **depois** apagar os 6 daqui com `terraform destroy -target=...`
(mesmos alvos do job `newrelic-legacy-apply` do `cd.yml`) e remover estes 2 arquivos.

## Aplicar manualmente

```bash
cd infra/terraform/aws
terraform init
terraform apply -var newrelic_api_key=<NRAK...> -var newrelic_account_id=<account_id> \
  -target=newrelic_alert_policy.os_falhas \
  -target=newrelic_nrql_alert_condition.os_erros_500 \
  -target=newrelic_notification_destination.email \
  -target=newrelic_notification_channel.email \
  -target=newrelic_workflow.os_falhas \
  -target=newrelic_one_dashboard.observabilidade_negocio
```

`newrelic_api_key`/`newrelic_account_id`: gere uma User API Key em Account settings → API keys na
New Relic (o account ID aparece na mesma tela). Usados só pelo provider Terraform, diferente da
license key do agente/Helm (`NEW_RELIC_LICENSE_KEY`, usada pelo repositório
`fiap-mecanica-infra-k8s` e pelo Secret Kubernetes da aplicação).

## Variáveis relevantes hoje (`vars.tf`)

| Variável | Default | Descrição |
|---|---|---|
| `project_name` | `fiap-mecanica` | Prefixo usado no nome dos recursos (inclusive os legados de VPC/EKS) |
| `region_default` | `us-east-1` | Região AWS |
| `tags` | `{Name = "fiap-mecanica-terraform"}` | Tags aplicadas aos recursos |
| `instance_type` | `t3.medium` | Só relevante para os arquivos legados de EKS (não aplicados por este CD) |
| `newrelic_api_key` | — (obrigatório, sensível) | User API Key da New Relic |
| `newrelic_account_id` | — (obrigatório) | Account ID numérico da New Relic |
| `alert_email` | `arthurkohl0@gmail.com` | E-mail que recebe os alertas de falha de processamento de OS |

`db_name`, `db_instance_class`, `db_username`, `db_password` foram removidas — não há mais banco
gerenciado daqui.

## Outputs (`output.tf`)

Além dos outputs legados de VPC/EKS (não usados por nada, mantidos só para o state ficar
consistente com o config), este módulo expõe `eks_cluster_security_group_id` — duplicando o
mesmo output do repositório `fiap-mecanica-infra-k8s`, pelo mesmo motivo de tudo aqui: enquanto
os dois configs apontarem para a mesma chave de state, precisam declarar exatamente os mesmos
outputs, senão `terraform plan` mostraria diferença.
