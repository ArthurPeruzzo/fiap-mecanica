# RFC-0001 — Escolha da nuvem: AWS

- **Relacionado:** [ADR-0004](../adr/0004-infra-quatro-repositorios.md)

## Resumo

A solução é implantada na **AWS**. O fator decisivo é a conta **AWS Academy Lab** fornecida pelo
curso, sem custo; somado a isso, toda a stack escolhida (Terraform, New Relic via OTLP, GitHub
Actions) tem suporte maduro para AWS e a equipe já tem familiaridade com os serviços usados
(EKS, RDS, Lambda, API Gateway).

## Contexto e motivação

O enunciado deixa a nuvem livre, mas exige: cluster Kubernetes com escalabilidade, banco
gerenciado, Function serverless para autenticação, API Gateway, provisionamento por Terraform e
integração com ferramenta de observabilidade. É um projeto acadêmico, com orçamento efetivamente
zero fora do que o curso provê.

## Opções consideradas

### AWS (recomendada)

**Prós**
- Conta Academy Lab provida pelo curso — sem custo, sem cartão de crédito.
- Serviços cobrem tudo que o enunciado pede: EKS, RDS for MySQL, Lambda, API Gateway HTTP API v2,
  ECR, CloudWatch.
- Provider Terraform AWS é o mais maduro e documentado.
- New Relic tem endpoint OTLP e integração Kubernetes (`nri-bundle`) prontos para AWS/EKS.
- Familiaridade da equipe.

**Contras / restrições da Academy Lab**
- Conta **efêmera**: expira/rotaciona periodicamente e zera toda a infraestrutura — obriga a
  disciplina de "reconstruir do zero pelos CDs".
- Não permite criar IAM roles próprias — usa-se a `LabRole` existente (data source).
- Service Control Policies bloqueiam algumas chamadas (ex.: `GetBucketObjectLockConfiguration`,
  o que impede gerenciar o bucket de state como recurso Terraform).
- Sem orçamento para NAT Gateway → apenas subnets públicas; RDS single-AZ.

### Google Cloud (GKE / Cloud SQL / Cloud Functions / API Gateway)

**Prós:** stack equivalente e competente; GKE Autopilot é conveniente.
**Contras:** sem conta provida pelo curso → custo do próprio bolso; menos familiaridade;
retrabalho para migrar o que já estava na AWS desde a Fase 2.

### Azure (AKS / Azure SQL / Functions / API Management)

**Prós:** stack equivalente.
**Contras:** mesmo problema de custo; API Management é mais pesado e caro que um HTTP API v2 para
o escopo; menos familiaridade.

### Local / on-premises (kind, minikube, Postgres em container)

**Contras:** não atende "deploy automático para a nuvem"; sem serviços gerenciados.

## Recomendação

**AWS.** O custo zero via Academy Lab é determinante para um projeto acadêmico, e os serviços
atendem o enunciado sem lacunas. As restrições da conta Lab são contornáveis e já estão
documentadas.

## Trade-offs e riscos

- **Efemeridade da conta**: mitigada tratando "derrubar e reconstruir do zero" como caminho
  normal — os 4 CDs se auto-inicializam (inclusive criando o bucket de state). Já validado.
- **`LabRole` única** para EKS e Lambda: menos princípio do menor privilégio; aceitável no
  escopo.
- **SCP inesperada** pode quebrar um recurso Terraform sem aviso (aconteceu com `aws_s3_bucket`).
  Mitigação pontual: tirar o recurso do Terraform e criá-lo por CLI idempotente no CD.
- **Região única (`us-east-1`), RDS single-AZ, sem NAT**: postura de custo, não de produção.
- **Lock-in**: baixo — o código não usa nada específico de AWS além do que o Terraform abstrai;
  o banco não usa recurso proprietário (ver [RFC-0002](0002-escolha-do-banco.md)).

## Consequências

- Infraestrutura em Terraform, dividida em 4 repositórios ([ADR-0004](../adr/0004-infra-quatro-repositorios.md)).
- Bucket de state Terraform criado fora do Terraform (bootstrap por CLI no CD).
- Rede só com subnets públicas; RDS acessível apenas pelo security group do cluster.
- `skip_final_snapshot = true` e `deletion_protection = false` no RDS — coerente com a
  efemeridade da conta.
