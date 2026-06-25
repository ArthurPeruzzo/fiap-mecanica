package com.fiap.mecanica.ordemdeservico.core.usecase.servico;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarServicoDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;

public class CriarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public CriarServicoUseCase(ServicoGateway servicoGateway) {
        this.servicoGateway = servicoGateway;
    }

    public void criar(CriarServicoDto dto) {
        var servico = new Servico(dto.nome(), dto.descricao(), dto.preco());
        servicoGateway.criar(servico);
    }
}
