# API Gateway — fiap-mecanica (Terraform)

Provisiona o **AWS API Gateway HTTP API (v2)** que serve de porta de entrada da aplicação. Roteia
todo o tráfego para o LoadBalancer do Service Kubernetes (`k8s/service.yaml`) via integração
`HTTP_PROXY`, e deixa o terreno preparado para a Function Lambda de autenticação
(`fiap-mecanica-lambda`) entrar como uma rota adicional, sem acoplamento.

## Fluxo (macro)

```mermaid
graph LR
    User["Cliente / Postman / Swagger"]

    subgraph APIGW["API Gateway · HTTP API v2"]
        STAGE["stage $default<br/>(sem prefixo de path)"]
        R1["ANY /{proxy+}"]
        R2["POST /auth/cliente<br/>(futuro · Fase 3)"]
    end

    subgraph EKS["EKS · namespace fiap-mecanica"]
        ELB["Service LoadBalancer<br/>(ELB :80)"]
        PODS["Pods da app"]
    end

    LAMBDA["Lambda fiap-mecanica-auth<br/>(futuro)"]

    User -->|HTTPS| STAGE
    STAGE --> R1 & R2
    R1 -->|HTTP_PROXY| ELB --> PODS
    R2 -.->|AWS_PROXY| LAMBDA
    LAMBDA -.->|GET /authenticate/cliente/status| ELB
```

## Por que um root module separado

O state deste módulo (`tfstate/apigateway.tfstate`) é **independente** do state da infra principal
(`../aws`, `tfstate/terraform.tfstate`). Três motivos concretos:

1. **Ordem de dependência.** A integração precisa do hostname do ELB, que só existe depois do
   `k8s-deploy` — ou seja, depois do `terraform apply` do módulo `../aws`. No mesmo state, seria
   preciso aplicar o state inteiro (EKS + RDS + New Relic) **duas vezes por deploy** só para
   atualizar uma rota.
2. **Desacoplamento da Lambda.** Um `data "aws_lambda_function"` no state principal faria todo
   `plan`/`apply` falhar enquanto a Lambda não existisse — quebrando o CI em Pull Requests e
   impossibilitando um deploy do zero.
3. **Estabilidade da URL pública.** Um `terraform destroy` da infra principal não derruba o
   gateway, então a URL do `execute-api` sobrevive à recriação de VPC/EKS/RDS.

## Recursos criados

| Arquivo | Recurso | O que é |
|---|---|---|
| `main.tf` | `aws_apigatewayv2_api.gateway` | HTTP API `fiap-mecanica-api` |
| `main.tf` | `aws_apigatewayv2_stage.default` | Stage `$default` com `auto_deploy` — ver "Por que `$default`" abaixo |
| `main.tf` | `aws_apigatewayv2_integration.app` | Integração `HTTP_PROXY` → `http://<app_elb_host>/{proxy}`, timeout 30s. Criada só quando `app_elb_host != ""` |
| `main.tf` | `aws_apigatewayv2_route.app_proxy` | Rota `ANY /{proxy+}`. Criada só quando `app_elb_host != ""` |
| `main.tf` | `data.aws_lambda_function.auth` | Referência à Function Lambda, criada no repo `fiap-mecanica-lambda`. Só avaliada quando `auth_lambda_name != ""` |
| `main.tf` | `aws_apigatewayv2_integration.auth` | Integração `AWS_PROXY` → Lambda, `payload_format_version = "1.0"`, timeout 29s |
| `main.tf` | `aws_apigatewayv2_route.auth` | Rota `POST /auth/cliente` |
| `main.tf` | `aws_lambda_permission.apigw` | Autoriza este gateway a invocar a função. **Sem ela, o gateway devolve 500** |
| `backend.tf` | — | Backend `s3` (bucket `fiap-mecanica`, `tfstate/apigateway.tfstate`). O bucket é criado por `../aws/bucket.tf` — sem bootstrap próprio |
| `providers.tf` | — | Provider `aws` (`us-east-1`). O provider `newrelic` **não** é replicado aqui |

## Por que o stage `$default`

Com `$default` a URL fica `https://<id>.execute-api.us-east-1.amazonaws.com/<path>`, **sem
prefixo**. Um stage nomeado (ex.: `prod`) entregaria `/prod/ordem-servico/...` ao backend; nenhum
matcher do `SecurityConfiguration` casaria e **tudo cairia em `anyRequest().denyAll()` → 403**. O
Swagger (`/swagger-ui/**`, `/v3/api-docs/**`) quebraria pelo mesmo motivo. `auto_deploy = true`
ainda dispensa o `aws_apigatewayv2_deployment`.

## Variáveis (`vars.tf`)

| Variável | Default | Descrição |
|---|---|---|
| `project_name` | `fiap-mecanica` | Prefixo do nome dos recursos |
| `region_default` | `us-east-1` | Região AWS |
| `tags` | `{Name = "fiap-mecanica-terraform"}` | Tags aplicadas aos recursos |
| `app_elb_host` | `""` | Hostname do ELB do Service K8s, sem esquema e sem barra final. Vazio ⇒ a rota da app não é criada e o gateway responde 404 em tudo |
| `auth_lambda_name` | `""` | Nome da Lambda de autenticação. Ainda não consumido — reservado para a Fase 3 |

Os defaults vazios são deliberados: permitem `terraform plan` no CI (em Pull Request não existe
ELB) e permitem criar o gateway numa infra do zero, antes do primeiro deploy no Kubernetes.

## Outputs (`output.tf`)

| Output | Uso |
|---|---|
| `api_gateway_url` | URL pública do gateway. Consumida pelo `cd.yml` para montar `URL_APROVAR_ORCAMENTO`/`URL_RECUSAR_ORCAMENTO` |
| `api_gateway_id` | ID da API — para montar a URL e consultar logs/métricas no CloudWatch |

## Ordem de deploy

O `cd.yml` já garante a ordem via `needs`:

```
terraform-apply (VPC/EKS/RDS/ECR)
  └─> docker-build-push
        └─> k8s-deploy   (cria o Service; o ELB ganha hostname)
              └─> apigw-apply   (recebe o hostname via TF_VAR_app_elb_host)
```

Manualmente:

```bash
ELB=$(kubectl get svc fiap-mecanica -n fiap-mecanica \
      -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

cd infra/terraform/apigateway
terraform init
terraform apply -var app_elb_host="$ELB"

API=$(terraform output -raw api_gateway_url)
curl -i -X POST "$API/authenticate/login" \
  -H 'Content-Type: application/json' \
  -d '{"cpf":"22255588846","senha":"MeCanica2026!@#"}'
```

Aplicar antes do Service existir cria o gateway **sem rotas** (404 em tudo). Não é erro: basta
reaplicar depois passando `-var app_elb_host=...`.

## Limitações conhecidas

- **O gateway pode ser contornado.** O ELB do Service continua público, então
  `http://<elb-host>/...` responde direto, sem passar pelo gateway. Fechar isso exigiria um NLB
  interno + `aws_apigatewayv2_vpc_link`, o que colide com a VPC só-de-subnets-públicas
  (`../aws/subnet.tf`) e com permissões incertas da conta AWS Academy Lab. Aceito
  conscientemente no escopo deste projeto.
- **O Swagger gera URLs apontando para o ELB.** Em `HTTP_PROXY` o API Gateway envia ao backend o
  header `Host` do **alvo** (o ELB), não o do `execute-api`. O springdoc monta os `servers` do
  `/v3/api-docs` a partir desse `Host`, então o "Try it out" dispara contra o ELB. Cosmético, sem
  impacto funcional. Correção, se incomodar: `springdoc.server-base-url` ou
  `server.forward-headers-strategy=FRAMEWORK`.
- **Sem access logs.** `access_log_settings` no stage exige `logs:CreateLogDelivery`, permissão
  não confirmada na conta do Lab. Deixado de fora para não confundir uma falha de permissão de
  log com uma falha do gateway; é um incremento isolado quando houver necessidade.
- **Timeout de 30s.** É o teto do HTTP API, não é configurável para além disso. Relevante no cold
  start da JVM logo após um rollout.
- **Sem domínio próprio.** A URL do `execute-api` já é estável (depende só do ID da API, que não
  muda quando o ELB é recriado) e já traz TLS válido — o ELB, por contraste, é HTTP puro na porta
  80. Um domínio custaria hosted zone Route 53 + registro + certificado ACM, com `route53:*`/ACM
  sendo permissões incertas no Lab. Se virar requisito, é aditivo:
  `aws_apigatewayv2_domain_name` + `aws_apigatewayv2_api_mapping`.

## Fase 3 — a rota da Lambda

`POST /auth/cliente` encaminha para a Function Lambda `fiap-mecanica-auth`, que é criada no
repositório **`fiap-mecanica-lambda`** (state próprio, `tfstate/lambda.tfstate`). Aqui ela é
apenas referenciada por nome, através de `auth_lambda_name`.

Tudo relacionado à Lambda fica desligado enquanto `auth_lambda_name` estiver vazio — inclusive o
`data "aws_lambda_function"`, que nem chega a ser avaliado. É isso que permite um `plan`/`apply`
deste módulo numa conta onde a função ainda não existe.

Quem preenche a variável é o job `apigw-apply` do `cd.yml`, **só depois de confirmar que a função
existe**:

```bash
if aws lambda get-function --function-name fiap-mecanica-auth >/dev/null 2>&1; then
  echo "TF_VAR_auth_lambda_name=fiap-mecanica-auth" >> "$GITHUB_ENV"
fi
```

Sem essa checagem, uma infra do zero quebraria: o CD da aplicação falharia inteiro por causa de um
componente satélite que ainda não subiu.

`POST /auth/cliente` é mais específica que `ANY /{proxy+}`, e o HTTP API prioriza a rota mais
específica — as duas convivem sem conflito. O path `/auth/**` não existe na aplicação (ela expõe
`/authenticate/**`), então não há colisão.

### Cadeia de timeouts
```
HTTP da Lambda (15s)  <  timeout da Lambda (25s)  <  integração (29s)  <  teto do HTTP API (30s)
```

### O endereço da aplicação na Lambda
A Lambda chama a aplicação **direto no ELB**, não pelo gateway — um hop a menos, e evita a chamada
voltar pelo gateway que a invocou. Como o hostname do ELB muda a cada recriação do Service, o job
`apigw-apply` também repassa esse valor para a função (`aws lambda update-function-configuration`),
fazendo merge com as variáveis existentes para não apagar o `JWT_SECRET`.

### Operação no dia a dia — os dois CDs são independentes

Um deploy de código novo na Lambda **não exige rodar este pipeline**. A integração aponta para a
função pelo ARN, que é derivado do nome e não muda quando só o código é publicado; e o
`data "aws_lambda_function"` não usa `qualifier`, então resolve para `$LATEST`. O gateway passa a
entregar a versão nova sozinho.

Este pipeline precisa rodar de novo só nestes casos:

| Situação | Motivo |
|---|---|
| Primeira vez | A rota só é criada depois que a função existe |
| A função foi **destruída e recriada** | A `aws_lambda_permission` vive no *resource policy* da função e se perde junto. Sem ela, o gateway devolve **500** ao invocar |
| A função foi **renomeada** | `auth_lambda_name` aponta para o nome antigo |

> **Sintoma reconhecível:** `500` em `POST /auth/cliente` logo após mexer na função. É a permissão
> que se perdeu — este pipeline a recria.

A permissão fica deste lado, e não no módulo da Lambda, porque precisa do `execution_arn` do
gateway (state daqui) **e** do nome da função (state de lá). Colocá-la lá criaria dependência
circular entre os dois states.

Na direção contrária não há acoplamento nenhum: o job `apigw-apply` checa `aws lambda get-function`
e, se a função não existir, apenas avisa e segue — o deploy da aplicação nunca é bloqueado pela
Lambda.

> ⚠️ **O JWT authorizer nativo do gateway não serve para este projeto.**
> `aws_apigatewayv2_authorizer` do tipo `JWT` só aceita **OIDC/JWKS (RS256 com issuer discovery)**.
> O contrato atual entre a app e a Lambda é **HS256 com segredo compartilhado**, que o gateway não
> tem como validar nativamente. Se a autorização precisar sair do app e ir para o gateway, as
> opções são (a) **Lambda authorizer** do tipo `REQUEST`, ou (b) migrar o projeto para RS256 +
> endpoint JWKS. Decidir isso **antes** de escrever a authorizer.
>
> Hoje a autorização continua 100% no app (`UserAuthenticationFilter`) e o gateway apenas roteia,
> repassando o header `Authorization` intacto.
