package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public class OrcamentoRecusadoViaAtendenteUseCase extends OrcamentoRecusadoOrdemDeServicoUseCase {

    public OrcamentoRecusadoViaAtendenteUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                                 EstoqueComandosGateway estoqueComandosGateway,
                                                 TransacaoGateway transacaoGateway,
                                                 NotificacaoGateway notificacaoGateway) {
        super(ordemDeServicoGateway, estoqueComandosGateway, transacaoGateway, notificacaoGateway);
    }

    public void recusar(Long ordemDeServicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemDeServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);
        super.recusar(ordemDeServico);
    }
}
