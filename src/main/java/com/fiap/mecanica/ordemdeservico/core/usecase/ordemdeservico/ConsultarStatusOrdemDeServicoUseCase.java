package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.dto.ConsultarStatusOrdemDeServicoDto;
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
        var os = ordemDeServicoGateway.buscarPorId(id)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);
        outputPort.apresentar(new ConsultarStatusOrdemDeServicoDto(os.getId(), os.getStatus().name()));
    }
}
