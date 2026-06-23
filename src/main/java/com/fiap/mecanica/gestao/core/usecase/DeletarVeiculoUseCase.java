package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;

public class DeletarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public DeletarVeiculoUseCase(VeiculoGateway veiculoGateway) {
        this.veiculoGateway = veiculoGateway;
    }

    public void deletar(Long id) {
        veiculoGateway.buscarPorId(id)
                .orElseThrow(VeiculoNaoEncontradoException::new);

        veiculoGateway.deletar(id);
    }
}
