package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.LinkAprovacaoOrcamento;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.LinkAprovacaoOrcamentoInvalidoException;
import com.fiap.mecanica.ordemdeservico.core.exception.LinkAprovacaoOrcamentoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public class OrcamentoAprovadoViaTokenUseCase extends OrcamentoAprovadoOrdemDeServicoUseCase {

    private final LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;

    public OrcamentoAprovadoViaTokenUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                            NotificacaoGateway notificacaoGateway,
                                            LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway) {
        super(ordemDeServicoGateway, notificacaoGateway);
        this.linkAprovacaoOrcamentoGateway = linkAprovacaoOrcamentoGateway;
    }

    public void aprovar(String token) {
        LinkAprovacaoOrcamento link = linkAprovacaoOrcamentoGateway.buscarPorToken(token)
                .orElseThrow(LinkAprovacaoOrcamentoNaoEncontradoException::new);

        if (!link.estaValido()) {
            throw new LinkAprovacaoOrcamentoInvalidoException();
        }

        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(link.getOrdemDeServicoId())
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        super.aprovar(ordemDeServico);

        link.marcarComoUtilizado();
        linkAprovacaoOrcamentoGateway.atualizar(link);
    }
}
