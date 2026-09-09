variable "project_name" {
  default = "fiap-mecanica"
}

variable "region_default" {
  default = "us-east-1"
}

variable "tags" {
  default = {
    Name = "fiap-mecanica-terraform"
  }
}

variable "newrelic_api_key" {
  description = "User API Key da New Relic (NRAK...) — gerenciada em Account settings > API keys. Usada só pelo provider Terraform, diferente da license key do agente/Helm."
  type        = string
  sensitive   = true
}

variable "newrelic_account_id" {
  description = "Account ID numérico da New Relic"
  type        = string
}

variable "alert_email" {
  description = "E-mail que recebe os alertas de falha de processamento de OS"
  type        = string
  default     = "arthurkohl0@gmail.com"
}

variable "health_check_url" {
  description = <<-EOT
    URL completa do healthcheck (ex.: https://<api-gw>/actuator/health) que o monitor de uptime
    da New Relic vai pingar. O hostname do API Gateway muda a cada recriação, então NÃO fica
    fixado aqui — o job `newrelic-apply` do CD descobre via `aws apigatewayv2 get-apis` e passa
    em `TF_VAR_health_check_url`. Vazio (default) => o monitor e a condição de alerta de uptime
    não são criados (1ª passada do CD, antes do gateway existir).
  EOT
  type        = string
  default     = ""
}
