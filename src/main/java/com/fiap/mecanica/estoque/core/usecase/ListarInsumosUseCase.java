package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.dto.ListarInsumosDto;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;

public class ListarInsumosUseCase {

    private final InsumoGateway insumoGateway;
    private final ListarInsumosOutputPort outputPort;

    public ListarInsumosUseCase(InsumoGateway insumoGateway, ListarInsumosOutputPort outputPort) {
        this.insumoGateway = insumoGateway;
        this.outputPort = outputPort;
    }

    public void listar(ListarInsumosDto dto) {
        outputPort.apresentar(insumoGateway.listar(dto.page(), dto.size()));
    }
}
