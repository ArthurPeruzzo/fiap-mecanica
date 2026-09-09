locals {
  # Nome do monitor de uptime. Fixo (não depende do recurso existir) para poder ser referenciado
  # tanto aqui quanto nas queries do dashboard mesmo quando o monitor ainda não foi criado.
  uptime_monitor_name = "${var.project_name} - healthcheck"
}

resource "newrelic_alert_policy" "os_falhas" {
  name                = "${var.project_name}-os-falhas"
  incident_preference = "PER_CONDITION"
}

# Monitor de uptime (ping SIMPLE) no /actuator/health via API Gateway. count-gated: só é criado
# quando o CD descobre a URL do gateway e passa em TF_VAR_health_check_url (vazio na 1ª passada).
resource "newrelic_synthetics_monitor" "health" {
  count = var.health_check_url != "" ? 1 : 0

  name             = local.uptime_monitor_name
  type             = "SIMPLE"
  status           = "ENABLED"
  period           = "EVERY_5_MINUTES"
  uri              = var.health_check_url
  locations_public = ["US_EAST_1"]

  # o corpo de /actuator/health contém {"status":"UP",...} — checa conteúdo, não só o 200.
  validation_string         = "UP"
  verify_ssl                = true
  treat_redirect_as_failure = true
}

# Alerta quando o healthcheck falha (uptime). Mesma policy/notification do alerta de OS.
resource "newrelic_nrql_alert_condition" "uptime" {
  count = var.health_check_url != "" ? 1 : 0

  policy_id          = newrelic_alert_policy.os_falhas.id
  name               = "Healthcheck da aplicação falhando (uptime)"
  type               = "static"
  enabled            = true
  aggregation_window = 60

  nrql {
    query = "SELECT count(*) FROM SyntheticCheck WHERE monitorName = '${local.uptime_monitor_name}' AND result = 'FAILED'"
  }

  critical {
    operator              = "above"
    threshold             = 0
    threshold_duration    = 300
    threshold_occurrences = "at_least_once"
  }
}

resource "newrelic_nrql_alert_condition" "os_erros_500" {
  policy_id          = newrelic_alert_policy.os_falhas.id
  name               = "Falha no processamento de OS (5xx)"
  type               = "static"
  enabled            = true
  aggregation_window = 60

  nrql {
    query = "SELECT count(*) FROM Metric WHERE metricName = 'http.server.requests' AND uri LIKE '/ordem-servico%' AND (outcome = 'SERVER_ERROR' OR status = '500')"
  }

  critical {
    operator              = "above"
    threshold             = 0
    threshold_duration    = 60
    threshold_occurrences = "at_least_once"
  }
}

resource "newrelic_notification_destination" "email" {
  name = "${var.project_name}-email"
  type = "EMAIL"

  property {
    key   = "email"
    value = var.alert_email
  }
}

resource "newrelic_notification_channel" "email" {
  name           = "${var.project_name}-email-channel"
  type           = "EMAIL"
  destination_id = newrelic_notification_destination.email.id
  product        = "IINT"

  property {
    key   = "subject"
    value = "Alerta fiap-mecanica: {{issueTitle}}"
  }
}

resource "newrelic_workflow" "os_falhas" {
  name                  = "${var.project_name}-os-falhas-workflow"
  muting_rules_handling = "NOTIFY_ALL_ISSUES"

  issues_filter {
    name = "os-falhas-filter"
    type = "FILTER"

    predicate {
      attribute = "labels.policyIds"
      operator  = "EXACTLY_MATCHES"
      values    = [newrelic_alert_policy.os_falhas.id]
    }
  }

  destination {
    channel_id = newrelic_notification_channel.email.id
  }
}
