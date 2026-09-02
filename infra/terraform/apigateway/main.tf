# API Gateway HTTP API (v2) — porta de entrada da aplicacao.
#
# Fica num root module separado (state proprio, `tfstate/apigateway.tfstate`) porque depende do
# hostname do LoadBalancer do Kubernetes, que so existe DEPOIS do `k8s-deploy` — ou seja, depois
# do `terraform apply` do modulo `../aws`. Manter os dois no mesmo state exigiria aplicar o state
# inteiro duas vezes por deploy so pra atualizar uma rota.

resource "aws_apigatewayv2_api" "gateway" {
  name          = "${var.project_name}-api"
  description   = "Porta de entrada da aplicacao ${var.project_name}"
  protocol_type = "HTTP"
  tags          = var.tags
}

# Stage `$default`: a URL fica `https://<id>.execute-api.<regiao>.amazonaws.com/<path>`, sem
# prefixo nenhum. Um stage nomeado (ex: `prod`) entregaria `/prod/ordem-servico/...` ao backend,
# nenhum matcher do SecurityConfiguration casaria e tudo cairia no `anyRequest().denyAll()` -> 403.
# `auto_deploy` tambem dispensa o `aws_apigatewayv2_deployment`.
resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.gateway.id
  name        = "$default"
  auto_deploy = true
  tags        = var.tags
}

# ---------------------------------------------------------------------------
# Aplicacao Java (EKS) — integracao HTTP_PROXY para o ELB publico do Service.
# ---------------------------------------------------------------------------

resource "aws_apigatewayv2_integration" "app" {
  count = var.app_elb_host != "" ? 1 : 0

  api_id             = aws_apigatewayv2_api.gateway.id
  integration_type   = "HTTP_PROXY"
  integration_method = "ANY"
  integration_uri    = "http://${var.app_elb_host}/{proxy}"

  # HTTP_PROXY so aceita 1.0. O 2.0 vale para integracoes AWS_PROXY (Lambda).
  payload_format_version = "1.0"

  # 30s e o teto do HTTP API. Relevante no cold start da JVM apos um rollout.
  timeout_milliseconds = 30000
}

# `ANY /{proxy+}` cobre todo verbo e todo path com pelo menos um segmento. Nao casa a raiz `/`,
# de proposito: `/` cai em `anyRequest().denyAll()` no app de qualquer forma, entao uma rota `ANY /`
# so serviria pra devolver 403 mais rapido.
resource "aws_apigatewayv2_route" "app_proxy" {
  count = var.app_elb_host != "" ? 1 : 0

  api_id    = aws_apigatewayv2_api.gateway.id
  route_key = "ANY /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.app[0].id}"
}
