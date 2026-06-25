package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;

public class ConsultarStatusOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ConsultarStatusOrdemDeServicoOutputPort outputPort;

    public ConsultarStatusOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                                 ConsultarStatusOrdemDeServicoOutputPort outputPort) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.outputPort = outputPort;
    }

    public void consultar(Long id) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(id)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);
        outputPort.apresentar(ordemDeServico);
    }
}
