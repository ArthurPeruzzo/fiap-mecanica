# Manifests Kubernetes — fiap-mecanica

Assume um cluster EKS já provisionado (`infra/terraform/aws/`) e o RDS MySQL já aplicado. A aplicação roda no cluster; o banco fica fora (RDS gerenciado).

## Pendência antes do primeiro apply

O `image:` em `deployment.yaml` já aponta para o repositório ECR real (`423972067332.dkr.ecr.us-east-1.amazonaws.com/fiap-mecanica`), mas a tag `:latest` só existe depois de um primeiro `docker push`. Sem isso o Pod fica em `ImagePullBackOff`.

## Passo a passo

0. **Configurar o `kubectl` para falar com o cluster EKS** (uma vez por sessão de credenciais da AWS Academy Lab, que expiram):
   ```bash
   aws eks update-kubeconfig --name eks-fiap-mecanica --region us-east-1
   ```

1. **Build + push da imagem para o ECR**:
   ```bash
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 423972067332.dkr.ecr.us-east-1.amazonaws.com
   docker build -t 423972067332.dkr.ecr.us-east-1.amazonaws.com/fiap-mecanica:latest .
   docker push 423972067332.dkr.ecr.us-east-1.amazonaws.com/fiap-mecanica:latest
   ```

2. **Preencher o `secret.yaml`** (nunca commitar — está no `.gitignore`):
   ```bash
   cp k8s/secret.yaml.example k8s/secret.yaml
   # editar k8s/secret.yaml com DB_USERNAME / DB_PASSWORD / JWT_SECRET reais
   ```

   `DB_URL` no `configmap.yaml` já está preenchido com o endpoint real do RDS (`fiap-mecanica-db.crg6wdidxgew.us-east-1.rds.amazonaws.com:3306`).

3. **Aplicar tudo, nessa ordem exata** — `namespace.yaml` precisa existir antes de qualquer recurso namespaced ser criado nele:
   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/metrics-server.yaml
   kubectl apply -f k8s/configmap.yaml -f k8s/secret.yaml
   kubectl apply -f k8s/deployment.yaml
   kubectl apply -f k8s/service.yaml
   kubectl apply -f k8s/hpa.yaml
   ```
   **Não use o atalho `kubectl apply -f k8s/`** num cluster do zero: ele processa os arquivos em ordem alfabética, e `configmap.yaml` viria antes de `namespace.yaml`, falhando porque o namespace ainda não existe. É por isso que o job `k8s-deploy` do pipeline (`.github/workflows/pipeline.yml`) também usa comandos explícitos nessa mesma ordem, não o atalho.

4. **Atualizar as URLs de orçamento** depois que o LoadBalancer subir:
   ```bash
   kubectl get svc fiap-mecanica -n fiap-mecanica
   # copiar o EXTERNAL-IP/hostname para URL_APROVAR_ORCAMENTO e URL_RECUSAR_ORCAMENTO
   # em k8s/configmap.yaml, depois:
   kubectl apply -f k8s/configmap.yaml
   kubectl rollout restart deployment/fiap-mecanica -n fiap-mecanica
   ```

## Ciclo rápido de teste (enquanto a versão da imagem for `:latest`)

Sem precisar editar nenhum YAML a cada iteração:
```bash
docker build -t 423972067332.dkr.ecr.us-east-1.amazonaws.com/fiap-mecanica:latest .
docker push 423972067332.dkr.ecr.us-east-1.amazonaws.com/fiap-mecanica:latest
kubectl rollout restart deployment/fiap-mecanica -n fiap-mecanica
```
(rodar `aws ecr get-login-password ... | docker login ...` de novo se a sessão de credenciais da AWS Academy Lab tiver expirado)
O `rollout restart` é necessário porque o Deployment não detecta sozinho que o conteúdo por trás da tag `:latest` mudou — ele só recria os pods quando o spec muda ou quando forçado. Com `imagePullPolicy: Always` (já definido em `deployment.yaml`), os pods recriados puxam a imagem nova.

Em produção (via pipeline), a tag deixa de ser `:latest` — vira a versão do `pom.xml` (ex: `1.0.0`), publicada automaticamente pelo job `docker-build-push` a cada push na `main`. Esse ciclo rápido com `:latest` continua útil só para teste manual local, fora da pipeline.

## Metrics Server

`k8s/metrics-server.yaml` é uma cópia estática do manifest oficial do [kubernetes-sigs/metrics-server](https://github.com/kubernetes-sigs/metrics-server). Roda em `kube-system`, não no namespace `fiap-mecanica` — é um add-on do cluster, não da aplicação. Sem ele o HPA (`kubectl get hpa -n fiap-mecanica`) mostra `<unknown>` nos targets de CPU/memória e nunca escala. Para atualizar a versão, baixe novamente de:
```bash
curl -fsSL -o k8s/metrics-server.yaml https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

## Arquivos

| Arquivo | O que é |
|---|---|
| `namespace.yaml` | Namespace `fiap-mecanica` |
| `configmap.yaml` | Config não sensível (DB_URL, URLs de orçamento) |
| `secret.yaml.example` | Template do Secret (credenciais DB, JWT). Copiar para `secret.yaml` e preencher — não commitar |
| `deployment.yaml` | Deployment da app, probes de liveness/readiness via Actuator, requests/limits de CPU/memória |
| `service.yaml` | Service `LoadBalancer`, expõe a porta 80 → 8080 |
| `hpa.yaml` | HorizontalPodAutoscaler (CPU e memória, 70%, 1–4 réplicas) |
| `metrics-server.yaml` | Add-on de cluster necessário para o HPA funcionar no EKS |
