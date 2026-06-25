package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.LinkAprovacaoOrcamento;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrcamentoEnviadoFactory;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public class EnviarOrcamentoOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final String urlAprovarOrcamento;
    private final String urlRecusarOrcamento;

    public EnviarOrcamentoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                                 LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway,
                                                 NotificacaoGateway notificacaoGateway,
                                                 String urlAprovarOrcamento,
                                                 String urlRecusarOrcamento) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.linkAprovacaoOrcamentoGateway = linkAprovacaoOrcamentoGateway;
        this.notificacaoGateway = notificacaoGateway;
        this.urlAprovarOrcamento = urlAprovarOrcamento;
        this.urlRecusarOrcamento = urlRecusarOrcamento;
    }

    public void enviar(Long ordemServicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        ordemDeServico.gravarEnvioOrcamento();
        ordemDeServicoGateway.atualizar(ordemDeServico);

        LinkAprovacaoOrcamento link = new LinkAprovacaoOrcamento(ordemServicoId);
        linkAprovacaoOrcamentoGateway.salvar(link);

        var params = MensagemParams.builder()
                .clienteId(ordemDeServico.getClienteId())
                .ordemId(ordemServicoId)
                .valorTotal(ordemDeServico.getOrcamento().valorTotal())
                .token(link.getToken())
                .urlAprovar(urlAprovarOrcamento)
                .urlRecusar(urlRecusarOrcamento)
                .build();
        notificacaoGateway.enviar(new MensagemOrcamentoEnviadoFactory().criar(params));
    }
}
