package com.fiap.mecanica.shared.metricas.core.gateway;

import com.fiap.mecanica.shared.metricas.core.domain.FaseOrdemDeServico;

import java.time.Duration;

public interface MetricasGateway {
    void registrarOrdemCriada();

    void registrarDuracaoFase(FaseOrdemDeServico fase, Duration duracao);
}
