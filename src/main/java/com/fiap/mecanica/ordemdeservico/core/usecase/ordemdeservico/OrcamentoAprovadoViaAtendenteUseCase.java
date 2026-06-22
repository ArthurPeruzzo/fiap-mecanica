package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import org.springframework.stereotype.Service;

@Service
public class OrcamentoAprovadoViaAtendenteUseCase extends OrcamentoAprovadoOrdemDeServicoUseCase {

    public OrcamentoAprovadoViaAtendenteUseCase(OrdemDeServicoGateway ordemDeServicoGateway) {
        super(ordemDeServicoGateway);
    }

    public void aprovar(Long ordemDeServicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemDeServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        super.aprovar(ordemDeServico);
    }
}
