package com.fiap.mecanica.ordemdeservico.core.usecase.servico;

import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeletarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public void deletar(Long id) {
        servicoGateway.buscarPorId(id).orElseThrow(ServicoNaoEncontradoException::new);
        servicoGateway.deletar(id);
    }
}
