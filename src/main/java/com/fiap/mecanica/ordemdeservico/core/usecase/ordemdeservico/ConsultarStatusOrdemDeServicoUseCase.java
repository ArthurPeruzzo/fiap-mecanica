package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.dto.ConsultarStatusOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultarStatusOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;

    public ConsultarStatusOrdemDeServicoDto consultar(Long id) {
        var os = ordemDeServicoGateway.buscarPorId(id)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);
        return new ConsultarStatusOrdemDeServicoDto(os.getId(), os.getStatus().name());
    }
}
