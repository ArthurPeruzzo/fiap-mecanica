output "api_gateway_url" {
  description = "URL publica do gateway (stage $default, sem prefixo de path). Estavel entre deploys: depende so do ID da API, nao do ELB."
  value       = aws_apigatewayv2_api.gateway.api_endpoint
}

output "api_gateway_id" {
  description = "ID da API — usado para montar a URL e para consultar logs/metricas no CloudWatch."
  value       = aws_apigatewayv2_api.gateway.id
}
