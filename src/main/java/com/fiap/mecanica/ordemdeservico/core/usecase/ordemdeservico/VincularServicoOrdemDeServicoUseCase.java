package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VincularServicoOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ServicoGateway servicoGateway;

    public void vincular(Long ordemServicoId, Long servicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        var servico = servicoGateway.buscarPorId(servicoId)
                .orElseThrow(ServicoNaoEncontradoException::new);

        ordemDeServico.vincularServico(servicoId, servico.getPreco());
        ordemDeServicoGateway.vincularServico(ordemServicoId, servicoId);
    }
}
