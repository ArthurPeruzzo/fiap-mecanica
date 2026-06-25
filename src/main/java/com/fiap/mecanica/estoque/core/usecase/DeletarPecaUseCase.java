package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;

public class DeletarPecaUseCase {

    private final PecaGateway pecaGateway;

    public DeletarPecaUseCase(PecaGateway pecaGateway) {
        this.pecaGateway = pecaGateway;
    }

    public void deletar(Long id) {
        pecaGateway.buscarPorId(id).orElseThrow(PecaNaoEncontradaException::new);
        pecaGateway.deletar(id);
    }
}
