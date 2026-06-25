package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public class OrcamentoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final PecaGateway pecaGateway;
    private final InsumoGateway insumoGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;
    private final String urlAprovarOrcamento;
    private final String urlRecusarOrcamento;

    public OrcamentoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                     PecaGateway pecaGateway,
                                     InsumoGateway insumoGateway,
                                     NotificacaoGateway notificacaoGateway,
                                     LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway,
                                     String urlAprovarOrcamento,
                                     String urlRecusarOrcamento) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.pecaGateway = pecaGateway;
        this.insumoGateway = insumoGateway;
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
        new OrcamentoRecusadoViaAtendenteUseCase(ordemDeServicoGateway, pecaGateway, insumoGateway, notificacaoGateway)
                .recusar(ordemServicoId);
    }

    public void recusarOrcamentoViaToken(String token) {
        new OrcamentoRecusadoViaTokenUseCase(ordemDeServicoGateway, pecaGateway, insumoGateway,
                notificacaoGateway, linkAprovacaoOrcamentoGateway).recusar(token);
    }

    public void aprovarOrcamento(Long ordemServicoId) {
        new OrcamentoAprovadoViaAtendenteUseCase(ordemDeServicoGateway, notificacaoGateway)
                .aprovar(ordemServicoId);
    }

    public void aprovarOrcamentoViaToken(String token) {
        new OrcamentoAprovadoViaTokenUseCase(ordemDeServicoGateway, notificacaoGateway, linkAprovacaoOrcamentoGateway)
                .aprovar(token);
    }
}
