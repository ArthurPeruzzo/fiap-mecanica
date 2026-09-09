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

    # Unidade em MINUTOS (não horas) e janela curta (3 dias, buckets de 30min) de propósito: as
    # fases de uma OS de demonstração duram minutos, não dias. Com a janela antiga (30 dias,
    # buckets de 1 dia) uma amostra recente virava uma fatia minúscula, praticamente invisível,
    # na ponta de um gráfico majoritariamente vazio — mesmo com dado real chegando. 1 dia era
    # curto demais: sem atividade nas últimas 24h o painel ficava vazio.
    # o exporter usa aggregation-temporality=delta, então average()/count() aqui são a média e
    # a contagem reais da janela (não o total acumulado desde o start do processo).
    widget_line {
      title  = "Tempo médio de execução por fase (minutos)"
      row    = 4
      column = 1
      width  = 12
      height = 3

      nrql_query {
        query = "SELECT average(os.duracao) / 60000 AS 'Minutos' FROM Metric WHERE service.name = '${local.newrelic_service_name}' FACET fase TIMESERIES 30 minutes SINCE 3 days ago"
      }
    }

    widget_table {
      title  = "Tempo médio por fase — detalhado (minutos)"
      row    = 7
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(os.duracao) / 60000 AS 'Tempo médio (min)', max(os.duracao) / 60000 AS 'Pior caso (min)', count(os.duracao) AS 'Amostras' FROM Metric WHERE service.name = '${local.newrelic_service_name}' FACET fase SINCE 3 days ago"
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
