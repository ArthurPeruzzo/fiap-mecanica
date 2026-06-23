package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.LinkAprovacaoOrcamento;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.LinkAprovacaoOrcamentoInvalidoException;
import com.fiap.mecanica.ordemdeservico.core.exception.LinkAprovacaoOrcamentoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import org.springframework.stereotype.Service;

@Service
public class OrcamentoRecusadoViaTokenUseCase extends OrcamentoRecusadoOrdemDeServicoUseCase {

    private final LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;

    public OrcamentoRecusadoViaTokenUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                             PecaGateway pecaGateway,
                                             InsumoGateway insumoGateway,
                                             NotificacaoGateway notificacaoGateway,
                                             LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway) {
        super(ordemDeServicoGateway, pecaGateway, insumoGateway, notificacaoGateway);
        this.linkAprovacaoOrcamentoGateway = linkAprovacaoOrcamentoGateway;
    }

    public void recusar(String token) {
        LinkAprovacaoOrcamento link = linkAprovacaoOrcamentoGateway.buscarPorToken(token)
                .orElseThrow(LinkAprovacaoOrcamentoNaoEncontradoException::new);

        if (!link.estaValido()) {
            throw new LinkAprovacaoOrcamentoInvalidoException();
        }

        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(link.getOrdemDeServicoId())
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        super.recusar(ordemDeServico);

        link.marcarComoUtilizado();
        linkAprovacaoOrcamentoGateway.atualizar(link);
    }
}
