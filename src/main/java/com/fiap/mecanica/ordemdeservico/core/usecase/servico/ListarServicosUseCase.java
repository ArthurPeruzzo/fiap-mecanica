package com.fiap.mecanica.ordemdeservico.core.usecase.servico;

import com.fiap.mecanica.ordemdeservico.core.dto.ListarServicosDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;

public class ListarServicosUseCase {

    private final ServicoGateway servicoGateway;
    private final ListarServicosOutputPort outputPort;

    public ListarServicosUseCase(ServicoGateway servicoGateway, ListarServicosOutputPort outputPort) {
        this.servicoGateway = servicoGateway;
        this.outputPort = outputPort;
    }

    public void listar(ListarServicosDto dto) {
        outputPort.apresentar(servicoGateway.listar(dto.page(), dto.size()));
    }
}
