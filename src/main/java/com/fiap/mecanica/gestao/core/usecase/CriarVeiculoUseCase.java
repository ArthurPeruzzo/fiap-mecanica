package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.dto.CriarVeiculoDto;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoJaExisteException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;

public class CriarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final ClienteGateway clienteGateway;

    public CriarVeiculoUseCase(VeiculoGateway veiculoGateway, ClienteGateway clienteGateway) {
        this.veiculoGateway = veiculoGateway;
        this.clienteGateway = clienteGateway;
    }

    public void criar(CriarVeiculoDto dto) {
        clienteGateway.buscarPorId(dto.clienteId())
                .orElseThrow(ClienteNaoEncontradoException::new);

        var veiculo = new Veiculo(dto.clienteId(), dto.placa(), dto.modelo(), dto.ano());

        if (veiculoGateway.existePorPlaca(veiculo.getPlaca().getValor())) {
            throw new VeiculoJaExisteException();
        }

        veiculoGateway.criar(veiculo);
    }
}
