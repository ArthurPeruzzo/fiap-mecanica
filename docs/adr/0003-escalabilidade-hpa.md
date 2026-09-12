# ADR-0003 — Escalabilidade horizontal com HPA (CPU 70%, 1–4 réplicas)

## Contexto

O enunciado exige "Cluster Kubernetes com escalabilidade". A aplicação é stateless (o estado
mora no RDS e o token é autocontido), então escalar é replicar o `Deployment`. A carga esperada
é baixa e intermitente (uso acadêmico / demonstração), e a conta AWS Academy Lab tem cota
apertada de instâncias.

## Decisão

Escalar por **`HorizontalPodAutoscaler` (`autoscaling/v2`)** sobre o `Deployment` da aplicação:

- `minReplicas: 1`, `maxReplicas: 4`.
- Métrica: **utilização de CPU, alvo 70%** (`Resource`/`cpu`/`Utilization`). Sem métrica de
  memória — a aplicação é CPU-bound sob carga e a JVM mantém a memória relativamente estável.
- `behavior`: subir rápido (janela de estabilização 15s, +2 pods a cada 30s) e descer devagar
  (janela 120s, −1 pod a cada 60s) para evitar *flapping*.
- Depende do **metrics-server**, instalado como add-on de cluster pelo CD do
  `fiap-mecanica-infra-k8s`.
- O node group EKS é `t3.medium`, 1–3 nós — teto físico compatível com 4 réplicas pequenas
  (requests `250m` CPU / `512Mi`, limits `500m` / `768Mi`).

## Consequências

**Positivas**
- Escalabilidade real e demonstrável sem custo fixo alto (fica em 1 réplica quando ocioso).
- Aplicação stateless → qualquer réplica atende qualquer requisição; o ELB distribui.
- Rollout sem downtime (probes de liveness/readiness no Actuator).

**Negativas / trade-offs**
- Se o CD do `fiap-mecanica-infra-k8s` não tiver rodado, o HPA fica com métricas `<unknown>` até
  o metrics-server existir (auto-corrige depois).
- Teto de 4 réplicas e node group de 1–3 nós são limites conscientes do ambiente Lab, não
  dimensionamento de produção.
- Escala reativa (a CPU precisa subir primeiro); picos muito bruscos veem latência antes do
  scale-up.
