# Manifests Kubernetes — fiap-mecanica

Assume um cluster EKS já provisionado (`infra/terraform/aws/`) e o RDS MySQL já aplicado. A aplicação roda no cluster; o banco fica fora (RDS gerenciado).

## Fluxo (macro)

```mermaid
graph TB
    User["Usuário / navegador"]
    APIGW["API Gateway (HTTP API v2)<br/>porta de entrada · infra/terraform/apigateway"]

    subgraph NS["Cluster EKS · namespace fiap-mecanica"]
        SVC["Service<br/>LoadBalancer :80 → 8080"]
        subgraph DEP["Deployment"]
            P1["Pod"]
            P2["Pod"]
        end
        HPA["HPA<br/>CPU/mem 70% · 1–4 pods"]
        CM["ConfigMap<br/>DB_URL · URLs"]
        SEC["Secret<br/>DB cred · JWT"]
        MS["Metrics Server"]
    end

    subgraph NR["namespace newrelic"]
        NRB["nri-bundle<br/>(infra + kube-state-metrics + Fluent Bit)"]
    end

    ECR["ECR"]
    RDS[("RDS MySQL")]
    NEWRELIC(["New Relic<br/>(OTLP + logs + infra K8s)"])

    User -->|HTTPS| APIGW -->|HTTP_PROXY| SVC --> P1 & P2
    User -.->|acesso direto ainda possível| SVC
    CM -->|env| DEP
    SEC -->|env| DEP
    MS -->|métricas| HPA
    HPA -->|escala| DEP
    ECR -->|imagem| DEP
    P1 -->|JDBC| RDS
    P1 -.->|traces/métricas OTLP| NEWRELIC
    NRB -->|CPU/mem dos nós/pods + logs| NEWRELIC
```

## Passo a passo

0. **Configurar o `kubectl` para falar com o cluster EKS** (uma vez por sessão de credenciais da AWS Academy Lab, que expiram):
   ```bash
   aws eks update-kubeconfig --name eks-fiap-mecanica --region us-east-1
   ```

1. **Build + push da imagem para o ECR**. O host do ECR inclui o *account ID* da conta AWS (muda se a conta da Lab for trocada), então em vez de fixá-lo, pegue a URL do repositório do output do Terraform:
   ```bash
   ECR=$(cd infra/terraform/aws && terraform output -raw ecr_repository_url)
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin "${ECR%/*}"
   docker build -t "$ECR:latest" .
   docker push "$ECR:latest"
   ```

2. **Preencher o `secret.yaml`** (nunca commitar — está no `.gitignore`):
   ```bash
   cp k8s/secret.yaml.example k8s/secret.yaml
   # editar k8s/secret.yaml com DB_USERNAME / DB_PASSWORD / JWT_SECRET reais
   ```

   Ajuste o `DB_URL` no `configmap.yaml` com o endpoint atual do RDS — ele **muda a cada recriação do banco**, por isso não fica fixado no repo. Pegue o valor atual com `terraform output -raw db_endpoint` no repositório `fiap-mecanica-infra-db`.

   Pré-requisito: o cluster precisa ter o **metrics-server** e a integração da New Relic já instalados — isso agora é feito pelo CD do repositório `fiap-mecanica-infra-k8s`, não daqui (ver "Metrics Server" e "New Relic" abaixo).

3. **Aplicar tudo, nessa ordem exata** — `namespace.yaml` precisa existir antes de qualquer recurso namespaced ser criado nele:
   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/configmap.yaml -f k8s/secret.yaml
   kubectl apply -f k8s/deployment.yaml
   kubectl apply -f k8s/service.yaml
   kubectl apply -f k8s/hpa.yaml
   ```
   **Não use o atalho `kubectl apply -f k8s/`** num cluster do zero: ele processa os arquivos em ordem alfabética, e `configmap.yaml` viria antes de `namespace.yaml`, falhando porque o namespace ainda não existe. É por isso que o job `k8s-deploy` do pipeline (`.github/workflows/cd.yml`) também usa comandos explícitos nessa mesma ordem, não o atalho.

4. **Atualizar as URLs de orçamento** depois que o LoadBalancer subir:
   ```bash
   kubectl get svc fiap-mecanica -n fiap-mecanica
   # copiar o EXTERNAL-IP/hostname para URL_APROVAR_ORCAMENTO e URL_RECUSAR_ORCAMENTO
   # em k8s/configmap.yaml, depois:
   kubectl apply -f k8s/configmap.yaml
   kubectl rollout restart deployment/fiap-mecanica -n fiap-mecanica
   ```

   **Isso é só para o fluxo manual.** O job `k8s-deploy` do `cd.yml` não usa o `configmap.yaml` commitado — ele gera o ConfigMap dinamicamente: `DB_URL` a partir do output do Terraform (`terraform output db_endpoint`, capturado automaticamente no job anterior) e as URLs de orçamento a partir do hostname real do LoadBalancer, obtido via `kubectl get svc` com espera ativa (poll) logo após aplicar o `Service` — sem precisar de passo manual, e funcionando mesmo depois de um `terraform destroy`/`apply` que gere um RDS e um LoadBalancer novos.

   **Depois disso, o job `apigw-apply` reescreve as URLs de orçamento apontando para o API Gateway.** Elas são os links enviados ao cliente final por notificação, e precisam passar pelo gateway, não pelo ELB. Só dá pra fazer nesse ponto do pipeline: a URL do gateway só é conhecida depois do `terraform apply` do módulo `infra/terraform/apigateway`, que por sua vez precisa do hostname do ELB gerado aqui. O job só reinicia os pods quando o valor realmente muda — como o ID do gateway é estável entre deploys, na prática só o primeiro deploy (ou uma recriação do gateway) paga esse rollout extra.

## Metrics Server e New Relic (Helm) — agora no repositório fiap-mecanica-infra-k8s

Migraram para lá porque são add-ons de **cluster**, não da aplicação — não mudam junto com o
código daqui, mudam junto com a infraestrutura do EKS. O `cd.yml` deste repositório não os aplica
mais.

Pré-requisito para um deploy funcionar de ponta a ponta (HPA reportando métricas, logs/traces
chegando na New Relic): o CD do `fiap-mecanica-infra-k8s` precisa ter rodado pelo menos uma vez
depois que o cluster existe. Numa infra do zero, isso significa: CD daquele repositório antes do
primeiro CD deste. Se a ordem for invertida, o HPA (`kubectl get hpa -n fiap-mecanica`) mostra
`<unknown>` até o metrics-server chegar — não é erro permanente, se autocorrige assim que o add-on
for instalado.

Detalhes de versão do metrics-server, dos três subcharts do `nri-bundle`
(`newrelic-infrastructure`, `kube-state-metrics`, `newrelic-logging`) e como verificar a
instalação: ver o README de `fiap-mecanica-infra-k8s`.

## Arquivos

| Arquivo | O que é |
|---|---|
| `namespace.yaml` | Namespace `fiap-mecanica` |
| `configmap.yaml` | Config não sensível (DB_URL, URLs de orçamento) — referência para uso manual; o `cd.yml` gera o seu próprio ConfigMap dinamicamente, não aplica este arquivo |
| `secret.yaml.example` | Template do Secret (credenciais DB, JWT). Copiar para `secret.yaml` e preencher — não commitar |
| `deployment.yaml` | Deployment da app, probes de liveness/readiness via Actuator, requests/limits de CPU/memória |
| `service.yaml` | Service `LoadBalancer`, expõe a porta 80 → 8080. É o alvo da integração `HTTP_PROXY` do API Gateway — o hostname do ELB é gerado pela AWS e **muda a cada recriação do Service**, por isso o `cd.yml` o descobre em runtime e repassa ao módulo `infra/terraform/apigateway` |
| `hpa.yaml` | HorizontalPodAutoscaler (CPU e memória, 70%, 1–4 réplicas) |

> `metrics-server.yaml` e a integração Kubernetes da New Relic (`nri-bundle`) não têm mais arquivo/passo aqui — ver "Metrics Server e New Relic (Helm)" acima.
