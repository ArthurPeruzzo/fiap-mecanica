package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.FinalizarServicoOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.IniciarServicoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public class ExecucaoServicoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ServicoGateway servicoGateway;
    private final NotificacaoGateway notificacaoGateway;

    public ExecucaoServicoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                          ServicoGateway servicoGateway,
                                          NotificacaoGateway notificacaoGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.servicoGateway = servicoGateway;
        this.notificacaoGateway = notificacaoGateway;
    }

    public void iniciarServico(Long ordemServicoId, Long servicoId) {
        new IniciarServicoOrdemDeServicoUseCase(ordemDeServicoGateway, servicoGateway)
                .iniciar(ordemServicoId, servicoId);
    }

    public void finalizarServico(Long ordemServicoId, Long servicoId) {
        new FinalizarServicoOrdemDeServicoUseCase(ordemDeServicoGateway, servicoGateway, notificacaoGateway)
                .finalizar(ordemServicoId, servicoId);
    }
}
