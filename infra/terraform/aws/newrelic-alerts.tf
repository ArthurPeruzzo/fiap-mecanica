resource "newrelic_alert_policy" "os_falhas" {
  name                = "${var.project_name}-os-falhas"
  incident_preference = "PER_CONDITION"
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
