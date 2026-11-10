package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.PagamentoComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public class OrcamentoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final PagamentoComandosGateway pagamentoComandosGateway;
    private final EstoqueComandosGateway estoqueComandosGateway;
    private final TransacaoGateway transacaoGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;
    private final String urlAprovarOrcamento;
    private final String urlRecusarOrcamento;

    public OrcamentoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                     PagamentoComandosGateway pagamentoComandosGateway,
                                     EstoqueComandosGateway estoqueComandosGateway,
                                     TransacaoGateway transacaoGateway,
                                     NotificacaoGateway notificacaoGateway,
                                     LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway,
                                     String urlAprovarOrcamento,
                                     String urlRecusarOrcamento) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.pagamentoComandosGateway = pagamentoComandosGateway;
        this.estoqueComandosGateway = estoqueComandosGateway;
        this.transacaoGateway = transacaoGateway;
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
        new OrcamentoRecusadoViaAtendenteUseCase(ordemDeServicoGateway, estoqueComandosGateway, transacaoGateway, notificacaoGateway)
                .recusar(ordemServicoId);
    }

    public void recusarOrcamentoViaToken(String token) {
        new OrcamentoRecusadoViaTokenUseCase(ordemDeServicoGateway, estoqueComandosGateway, transacaoGateway,
                notificacaoGateway, linkAprovacaoOrcamentoGateway).recusar(token);
    }

    public void aprovarOrcamento(Long ordemServicoId) {
        new OrcamentoAprovadoViaAtendenteUseCase(ordemDeServicoGateway, pagamentoComandosGateway, transacaoGateway, notificacaoGateway)
                .aprovar(ordemServicoId);
    }

    public void aprovarOrcamentoViaToken(String token) {
        new OrcamentoAprovadoViaTokenUseCase(ordemDeServicoGateway, pagamentoComandosGateway, transacaoGateway, notificacaoGateway, linkAprovacaoOrcamentoGateway)
                .aprovar(token);
    }
}
