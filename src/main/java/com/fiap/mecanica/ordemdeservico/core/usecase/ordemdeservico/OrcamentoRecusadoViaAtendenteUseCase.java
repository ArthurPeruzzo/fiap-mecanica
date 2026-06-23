package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import org.springframework.stereotype.Service;

@Service
public class OrcamentoRecusadoViaAtendenteUseCase extends OrcamentoRecusadoOrdemDeServicoUseCase {

    public OrcamentoRecusadoViaAtendenteUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                                 PecaGateway pecaGateway,
                                                 InsumoGateway insumoGateway,
                                                 NotificacaoGateway notificacaoGateway) {
        super(ordemDeServicoGateway, pecaGateway, insumoGateway, notificacaoGateway);
    }

    public void recusar(Long ordemDeServicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemDeServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);
        super.recusar(ordemDeServico);
    }
}
