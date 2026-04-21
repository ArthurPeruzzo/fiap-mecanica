package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.dto.AtualizarVeiculoDto;
import com.fiap.mecanica.gestao.core.exception.VeiculoJaExisteException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AtualizarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public void atualizar(AtualizarVeiculoDto dto) {
        var veiculo = veiculoGateway.buscarPorId(dto.id())
                .orElseThrow(VeiculoNaoEncontradoException::new);

        veiculo.atualizar(dto.placa(), dto.modelo(), dto.ano());

        if (veiculoGateway.existePorPlacaExcluindoId(veiculo.getPlaca().getValor(), dto.id())) {
            throw new VeiculoJaExisteException();
        }

        veiculoGateway.atualizar(veiculo);
    }
}
