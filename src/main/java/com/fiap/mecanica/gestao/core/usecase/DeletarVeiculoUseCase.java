package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeletarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public void deletar(Long id) {
        veiculoGateway.buscarPorId(id)
                .orElseThrow(VeiculoNaoEncontradoException::new);

        veiculoGateway.deletar(id);
    }
}
