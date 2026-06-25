package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;

public class DesvincularServicoOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ServicoGateway servicoGateway;

    public DesvincularServicoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                                    ServicoGateway servicoGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.servicoGateway = servicoGateway;
    }

    public void desvincular(Long ordemServicoId, Long servicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        servicoGateway.buscarPorId(servicoId)
                .orElseThrow(ServicoNaoEncontradoException::new);

        ordemDeServico.desvincularServico(servicoId);
        ordemDeServicoGateway.desvincularServico(ordemServicoId, servicoId);
    }
}
