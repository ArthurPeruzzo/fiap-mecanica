package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public class OrcamentoRecusadoViaAtendenteUseCase extends OrcamentoRecusadoOrdemDeServicoUseCase {

    public OrcamentoRecusadoViaAtendenteUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                                 ReservaEstoqueGateway reservaEstoqueGateway,
                                                 NotificacaoGateway notificacaoGateway) {
        super(ordemDeServicoGateway, reservaEstoqueGateway, notificacaoGateway);
    }

    public void recusar(Long ordemDeServicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemDeServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);
        super.recusar(ordemDeServico);
    }
}
