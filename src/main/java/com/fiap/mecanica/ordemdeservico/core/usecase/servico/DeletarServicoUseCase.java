package com.fiap.mecanica.ordemdeservico.core.usecase.servico;

import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;

public class DeletarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public DeletarServicoUseCase(ServicoGateway servicoGateway) {
        this.servicoGateway = servicoGateway;
    }

    public void deletar(Long id) {
        servicoGateway.buscarPorId(id).orElseThrow(ServicoNaoEncontradoException::new);
        servicoGateway.deletar(id);
    }
}
