package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.FinalizarServicoOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.IniciarServicoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public class ExecucaoServicoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ServicoGateway servicoGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final MetricasGateway metricasGateway;

    public ExecucaoServicoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                          ServicoGateway servicoGateway,
                                          NotificacaoGateway notificacaoGateway,
                                          MetricasGateway metricasGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.servicoGateway = servicoGateway;
        this.notificacaoGateway = notificacaoGateway;
        this.metricasGateway = metricasGateway;
    }

    public void iniciarServico(Long ordemServicoId, Long servicoId) {
        new IniciarServicoOrdemDeServicoUseCase(ordemDeServicoGateway, servicoGateway)
                .iniciar(ordemServicoId, servicoId);
    }

    public void finalizarServico(Long ordemServicoId, Long servicoId) {
        new FinalizarServicoOrdemDeServicoUseCase(ordemDeServicoGateway, servicoGateway, notificacaoGateway, metricasGateway)
                .finalizar(ordemServicoId, servicoId);
    }
}
