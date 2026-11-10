output "api_gateway_url" {
  description = "URL publica do gateway (stage $default, sem prefixo de path). Estavel entre deploys: depende so do ID da API, nao do ELB."
  value       = aws_apigatewayv2_api.gateway.api_endpoint
}

output "api_gateway_id" {
  description = "ID da API — usado para montar a URL e para consultar logs/metricas no CloudWatch."
  value       = aws_apigatewayv2_api.gateway.id
}

output "auth_route_ativa" {
  description = "Indica se a rota POST /auth/cliente (Function Lambda) foi criada. Falso enquanto auth_lambda_name estiver vazio."
  value       = var.auth_lambda_name != ""
}

output "estoque_rotas_ativas" {
  description = "Indica se as rotas /peca e /insumo (fiap-estoque) foram criadas. Falso enquanto estoque_elb_host estiver vazio."
  value       = var.estoque_elb_host != ""
}

output "cobranca_rotas_ativas" {
  description = "Indica se as rotas /pagamentos (fiap-cobranca) foram criadas. Falso enquanto cobranca_elb_host estiver vazio."
  value       = var.cobranca_elb_host != ""
}
