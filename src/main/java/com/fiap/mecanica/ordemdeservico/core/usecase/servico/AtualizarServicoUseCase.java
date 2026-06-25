package com.fiap.mecanica.ordemdeservico.core.usecase.servico;

import com.fiap.mecanica.ordemdeservico.core.dto.AtualizarServicoDto;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;

public class AtualizarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public AtualizarServicoUseCase(ServicoGateway servicoGateway) {
        this.servicoGateway = servicoGateway;
    }

    public void atualizar(AtualizarServicoDto dto) {
        var servico = servicoGateway.buscarPorId(dto.id()).orElseThrow(ServicoNaoEncontradoException::new);
        servico.atualizar(dto.nome(), dto.descricao(), dto.preco());
        servicoGateway.atualizar(servico);
    }
}
