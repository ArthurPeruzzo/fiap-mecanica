package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.dto.ListarPecasDto;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;

public class ListarPecasUseCase {

    private final PecaGateway pecaGateway;
    private final ListarPecasOutputPort outputPort;

    public ListarPecasUseCase(PecaGateway pecaGateway, ListarPecasOutputPort outputPort) {
        this.pecaGateway = pecaGateway;
        this.outputPort = outputPort;
    }

    public void listar(ListarPecasDto dto) {
        outputPort.apresentar(pecaGateway.listar(dto.page(), dto.size()));
    }
}
