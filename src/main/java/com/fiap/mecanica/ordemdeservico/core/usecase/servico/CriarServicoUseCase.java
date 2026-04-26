package com.fiap.mecanica.ordemdeservico.core.usecase.servico;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarServicoDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public void criar(CriarServicoDto dto) {
        var servico = new Servico(dto.nome(), dto.descricao(), dto.preco());
        servicoGateway.criar(servico);
    }
}
