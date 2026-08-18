# Manifests Kubernetes — fiap-mecanica

Assume um cluster EKS já provisionado (`infra/terraform/aws/`) e o RDS MySQL já aplicado. A aplicação roda no cluster; o banco fica fora (RDS gerenciado).

## Fluxo (macro)

```mermaid
graph TB
    User["Usuário / navegador"]

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

    User -->|HTTP| SVC --> P1 & P2
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

   Ajuste o `DB_URL` no `configmap.yaml` com o endpoint atual do RDS — ele **muda a cada recriação do banco**, por isso não fica fixado no repo. Pegue o valor atual com `terraform output -raw db_endpoint` (rodando em `infra/terraform/aws`).

3. **Aplicar tudo, nessa ordem exata** — `namespace.yaml` precisa existir antes de qualquer recurso namespaced ser criado nele:
   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/metrics-server.yaml
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

## Metrics Server

`k8s/metrics-server.yaml` é uma cópia estática do manifest oficial do [kubernetes-sigs/metrics-server](https://github.com/kubernetes-sigs/metrics-server). Roda em `kube-system`, não no namespace `fiap-mecanica` — é um add-on do cluster, não da aplicação. Sem ele o HPA (`kubectl get hpa -n fiap-mecanica`) mostra `<unknown>` nos targets de CPU/memória e nunca escala. Para atualizar a versão, baixe novamente de:
```bash
curl -fsSL -o k8s/metrics-server.yaml https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

## New Relic — Integração Kubernetes (Helm)

Diferente de tudo mais neste diretório, isso **não é aplicado com `kubectl apply -f`** — a New Relic só distribui oficialmente essa integração via Helm chart (`nri-bundle`), então o `cd.yml` faz `helm upgrade --install` direto, sem nenhum arquivo `.yaml` commitado aqui. Instala em um namespace próprio, `newrelic` (separado de `fiap-mecanica` e de `kube-system`, onde fica o metrics-server), via `--create-namespace`.

O bundle traz três peças, todas necessárias para cobrir o requisito de monitorar CPU/memória do cluster e, de brinde, enviar os logs estruturados (JSON/ECS) que a aplicação já escreve em stdout:
- `newrelic-infrastructure` (agente de infraestrutura, coleta CPU/memória de nós e pods — habilitado por padrão no chart, mas com `privileged=true` explícito para métricas completas de host)
- `kube-state-metrics` (visibilidade de objetos K8s — desabilitado por padrão no `nri-bundle`, precisa de `--set` explícito)
- `newrelic-logging` (Fluent Bit, embarca os logs de todos os pods para a New Relic — também desabilitado por padrão, precisa de `--set` explícito)

Para instalar manualmente (bootstrap de cluster do zero, mesma licença usada no `k8s/secret.yaml`):
```bash
helm repo add newrelic https://helm-charts.newrelic.com
helm repo update
helm upgrade --install newrelic-bundle newrelic/nri-bundle \
  --namespace newrelic --create-namespace \
  --set global.licenseKey="$NEW_RELIC_LICENSE_KEY" \
  --set global.cluster=eks-fiap-mecanica \
  --set global.lowDataMode=true \
  --set newrelic-infrastructure.privileged=true \
  --set kube-state-metrics.enabled=true \
  --set newrelic-logging.enabled=true \
  --wait --timeout 5m0s
```

O job `k8s-deploy` do `cd.yml` roda esse mesmo comando automaticamente a cada push na `main`, logo depois do metrics-server, usando o secret `NEW_RELIC_LICENSE_KEY` (GitHub Secrets) e a env `EKS_CLUSTER_NAME` já existentes no workflow — sem passo manual.

Para verificar: `kubectl get pods -n newrelic` (esperar os DaemonSets `newrelic-bundle-nrk8s-*`/Fluent Bit e o Deployment do `kube-state-metrics` como `Running`), e no New Relic, em **Kubernetes → Cluster explorer**, procurar pelo cluster `eks-fiap-mecanica`.

`global.account_id`/`NEW_RELIC_ACCOUNT_ID` **não é necessário** — o chart não tem essa chave em nenhum dos seus subcharts; a license key já associa os dados à conta certa no lado da New Relic.

## Arquivos

| Arquivo | O que é |
|---|---|
| `namespace.yaml` | Namespace `fiap-mecanica` |
| `configmap.yaml` | Config não sensível (DB_URL, URLs de orçamento) — referência para uso manual; o `cd.yml` gera o seu próprio ConfigMap dinamicamente, não aplica este arquivo |
| `secret.yaml.example` | Template do Secret (credenciais DB, JWT). Copiar para `secret.yaml` e preencher — não commitar |
| `deployment.yaml` | Deployment da app, probes de liveness/readiness via Actuator, requests/limits de CPU/memória |
| `service.yaml` | Service `LoadBalancer`, expõe a porta 80 → 8080 |
| `hpa.yaml` | HorizontalPodAutoscaler (CPU e memória, 70%, 1–4 réplicas) |
| `metrics-server.yaml` | Add-on de cluster necessário para o HPA funcionar no EKS |

> A integração Kubernetes da New Relic (`nri-bundle`) não tem arquivo aqui — é instalada via Helm, não `kubectl apply -f`. Ver seção "New Relic — Integração Kubernetes (Helm)" acima.
