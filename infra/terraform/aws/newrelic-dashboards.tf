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

  # ---------------------------------------------------------------------------------------------
  # Página 2: latência das APIs, uptime do healthcheck e consumo de CPU/memória do Kubernetes.
  # `http.server.requests` chega via OTLP (Micrometer) em milissegundos, como distribution —
  # percentile()/count() funcionam direto. As métricas K8s* vêm do nri-bundle (kube-state-metrics
  # + newrelic-infrastructure) instalado pelo CD do fiap-mecanica-infra-k8s. SyntheticCheck vem
  # do monitor de uptime (newrelic-alerts.tf), que só existe depois que o CD descobre a URL do
  # API Gateway — até lá os painéis de disponibilidade ficam sem dado.
  # ---------------------------------------------------------------------------------------------
  page {
    name = "Performance e Disponibilidade"

    widget_line {
      title  = "Latência das APIs — percentis (ms)"
      row    = 1
      column = 1
      width  = 8
      height = 3

      nrql_query {
        query = "SELECT percentile(http.server.requests, 50, 90, 95, 99) FROM Metric WHERE service.name = '${local.newrelic_service_name}' AND metricName = 'http.server.requests' AND uri NOT LIKE '/actuator%' TIMESERIES 5 minutes SINCE 6 hours ago"
      }
    }

    widget_billboard {
      title  = "Latência p95 (última hora, ms)"
      row    = 1
      column = 9
      width  = 4
      height = 3

      nrql_query {
        query = "SELECT percentile(http.server.requests, 95) AS 'p95 (ms)' FROM Metric WHERE service.name = '${local.newrelic_service_name}' AND metricName = 'http.server.requests' AND uri NOT LIKE '/actuator%' SINCE 1 hour ago"
      }
    }

    widget_table {
      title  = "Latência por rota"
      row    = 4
      column = 1
      width  = 12
      height = 3

      nrql_query {
        query = "SELECT percentile(http.server.requests, 50) AS 'p50 (ms)', percentile(http.server.requests, 95) AS 'p95 (ms)', percentile(http.server.requests, 99) AS 'p99 (ms)', count(http.server.requests) AS 'Reqs' FROM Metric WHERE service.name = '${local.newrelic_service_name}' AND metricName = 'http.server.requests' FACET uri, method SINCE 6 hours ago LIMIT 30"
      }
    }

    widget_billboard {
      title  = "Disponibilidade — healthcheck (24h)"
      row    = 7
      column = 1
      width  = 4
      height = 3

      nrql_query {
        query = "SELECT percentage(count(*), WHERE result = 'SUCCESS') AS 'Uptime %' FROM SyntheticCheck WHERE monitorName = '${local.uptime_monitor_name}' SINCE 24 hours ago"
      }
    }

    widget_line {
      title  = "Resultado do healthcheck ao longo do tempo"
      row    = 7
      column = 5
      width  = 8
      height = 3

      nrql_query {
        query = "SELECT count(*) FROM SyntheticCheck WHERE monitorName = '${local.uptime_monitor_name}' FACET result TIMESERIES 30 minutes SINCE 24 hours ago"
      }
    }

    widget_line {
      title  = "Kubernetes — CPU por pod (cores)"
      row    = 10
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(cpuUsedCores) FROM K8sContainerSample WHERE clusterName = 'eks-fiap-mecanica' AND namespaceName = 'fiap-mecanica' FACET podName TIMESERIES SINCE 3 hours ago"
      }
    }

    widget_line {
      title  = "Kubernetes — memória por pod (MB)"
      row    = 10
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(memoryUsedBytes) / 1e6 AS 'MB' FROM K8sContainerSample WHERE clusterName = 'eks-fiap-mecanica' AND namespaceName = 'fiap-mecanica' FACET podName TIMESERIES SINCE 3 hours ago"
      }
    }

    widget_billboard {
      title  = "Pods em execução (namespace fiap-mecanica)"
      row    = 13
      column = 1
      width  = 4
      height = 3

      nrql_query {
        query = "SELECT uniqueCount(podName) AS 'Pods Running' FROM K8sPodSample WHERE clusterName = 'eks-fiap-mecanica' AND namespaceName = 'fiap-mecanica' AND status = 'Running' SINCE 5 minutes ago"
      }
    }

    widget_line {
      title  = "Kubernetes — uso vs. limite (%) do container da app"
      row    = 13
      column = 5
      width  = 8
      height = 3

      nrql_query {
        query = "SELECT average(cpuCoresUtilization) AS 'CPU % do limite', average(memoryWorkingSetUtilization) AS 'Mem % do limite' FROM K8sContainerSample WHERE clusterName = 'eks-fiap-mecanica' AND namespaceName = 'fiap-mecanica' AND containerName = 'fiap-mecanica' TIMESERIES SINCE 3 hours ago"
      }
    }
  }
}
