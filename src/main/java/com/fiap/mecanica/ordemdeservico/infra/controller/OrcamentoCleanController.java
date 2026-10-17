package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public class OrcamentoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ReservaEstoqueGateway reservaEstoqueGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;
    private final String urlAprovarOrcamento;
    private final String urlRecusarOrcamento;

    public OrcamentoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                     ReservaEstoqueGateway reservaEstoqueGateway,
                                     NotificacaoGateway notificacaoGateway,
                                     LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway,
                                     String urlAprovarOrcamento,
                                     String urlRecusarOrcamento) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.reservaEstoqueGateway = reservaEstoqueGateway;
        this.notificacaoGateway = notificacaoGateway;
        this.linkAprovacaoOrcamentoGateway = linkAprovacaoOrcamentoGateway;
        this.urlAprovarOrcamento = urlAprovarOrcamento;
        this.urlRecusarOrcamento = urlRecusarOrcamento;
    }

    public void enviarOrcamento(Long ordemServicoId) {
        new EnviarOrcamentoOrdemDeServicoUseCase(ordemDeServicoGateway, linkAprovacaoOrcamentoGateway,
                notificacaoGateway, urlAprovarOrcamento, urlRecusarOrcamento).enviar(ordemServicoId);
    }

    public void recusarOrcamento(Long ordemServicoId) {
        new OrcamentoRecusadoViaAtendenteUseCase(ordemDeServicoGateway, reservaEstoqueGateway, notificacaoGateway)
                .recusar(ordemServicoId);
    }

    public void recusarOrcamentoViaToken(String token) {
        new OrcamentoRecusadoViaTokenUseCase(ordemDeServicoGateway, reservaEstoqueGateway,
                notificacaoGateway, linkAprovacaoOrcamentoGateway).recusar(token);
    }

    public void aprovarOrcamento(Long ordemServicoId) {
        new OrcamentoAprovadoViaAtendenteUseCase(ordemDeServicoGateway, reservaEstoqueGateway, notificacaoGateway)
                .aprovar(ordemServicoId);
    }

    public void aprovarOrcamentoViaToken(String token) {
        new OrcamentoAprovadoViaTokenUseCase(ordemDeServicoGateway, reservaEstoqueGateway, notificacaoGateway, linkAprovacaoOrcamentoGateway)
                .aprovar(token);
    }
}
