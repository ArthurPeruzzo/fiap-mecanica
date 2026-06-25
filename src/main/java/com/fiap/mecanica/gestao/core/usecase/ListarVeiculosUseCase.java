package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.dto.ListarVeiculosDto;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;

public class ListarVeiculosUseCase {

    private final VeiculoGateway veiculoGateway;
    private final ListarVeiculosOutputPort outputPort;

    public ListarVeiculosUseCase(VeiculoGateway veiculoGateway, ListarVeiculosOutputPort outputPort) {
        this.veiculoGateway = veiculoGateway;
        this.outputPort = outputPort;
    }

    public void listar(ListarVeiculosDto dto) {
        outputPort.apresentar(veiculoGateway.listar(dto.page(), dto.size()));
    }
}
