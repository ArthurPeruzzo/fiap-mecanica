package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.dto.ListarClientesDto;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;

public class ListarClientesUseCase {

    private final ClienteGateway clienteGateway;
    private final ListarClientesOutputPort outputPort;

    public ListarClientesUseCase(ClienteGateway clienteGateway, ListarClientesOutputPort outputPort) {
        this.clienteGateway = clienteGateway;
        this.outputPort = outputPort;
    }

    public void listar(ListarClientesDto dto) {
        outputPort.apresentar(clienteGateway.listar(dto.page(), dto.size()));
    }
}
