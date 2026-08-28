# service.name vem de spring.application.name (application.properties) — não do
# var.project_name (esse é o nome do projeto/infra, "fiap-mecanica"; o app se
# anuncia à New Relic como "mecanica").
locals {
  newrelic_service_name = "mecanica"
}

resource "newrelic_one_dashboard" "observabilidade_negocio" {
  name        = "${var.project_name} - Observabilidade de Negócio"
  permissions = "public_read_only"

  page {
    name = "Ordens de Serviço"

    widget_line {
      title  = "Volume diário de OS criadas"
      row    = 1
      column = 1
      width  = 12
      height = 3

      nrql_query {
        query = "SELECT sum(os.criadas) FROM Metric WHERE service.name = '${local.newrelic_service_name}' TIMESERIES 1 day SINCE 30 days ago"
      }
    }

    widget_line {
      title  = "Tempo médio de execução por fase (horas)"
      row    = 4
      column = 1
      width  = 12
      height = 3

      nrql_query {
        query = "SELECT average(os.duracao) / 3600000 AS 'Horas' FROM Metric WHERE service.name = '${local.newrelic_service_name}' FACET fase TIMESERIES 1 day SINCE 30 days ago"
      }
    }

    widget_table {
      title  = "Tempo médio por fase — detalhado"
      row    = 7
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(os.duracao) / 3600000 AS 'Tempo médio (h)', max(os.duracao) / 3600000 AS 'Pior caso (h)', count(os.duracao) AS 'Amostras' FROM Metric WHERE service.name = '${local.newrelic_service_name}' FACET fase SINCE 30 days ago"
      }
    }

    widget_billboard {
      title  = "Taxa de erro (24h)"
      row    = 7
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT percentage(count(http.server.requests), WHERE outcome = 'SERVER_ERROR' OR status LIKE '5%') AS 'Taxa de erro' FROM Metric WHERE service.name = '${local.newrelic_service_name}' SINCE 24 hours ago"
      }
    }

    widget_line {
      title  = "Erros e falhas nas integrações (5xx)"
      row    = 10
      column = 1
      width  = 12
      height = 3

      nrql_query {
        query = "SELECT count(http.server.requests) FROM Metric WHERE service.name = '${local.newrelic_service_name}' AND (outcome = 'SERVER_ERROR' OR status LIKE '5%') TIMESERIES 1 day SINCE 7 days ago"
      }
    }

    widget_table {
      title  = "Erros por rota"
      row    = 13
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT count(http.server.requests) AS 'Erros' FROM Metric WHERE service.name = '${local.newrelic_service_name}' AND (outcome = 'SERVER_ERROR' OR status LIKE '5%') FACET uri, status SINCE 7 days ago LIMIT 20"
      }
    }

    widget_table {
      title  = "Falhas de acesso ao banco (via logs)"
      row    = 13
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT count(*) FROM Log WHERE `log.logger` LIKE '%DatabaseGateway' AND `log.level` = 'ERROR' FACET `log.logger` SINCE 7 days ago"
      }
    }
  }
}
