package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import org.springframework.stereotype.Service;

@Service
public class OrcamentoRecusadoViaAtendenteUseCase extends OrcamentoRecusadoOrdemDeServicoUseCase {

    public OrcamentoRecusadoViaAtendenteUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                                 PecaGateway pecaGateway,
                                                 InsumoGateway insumoGateway) {
        super(ordemDeServicoGateway, pecaGateway, insumoGateway);
    }

    public void recusar(Long ordemDeServicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemDeServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);
        super.recusar(ordemDeServico);
    }
}
