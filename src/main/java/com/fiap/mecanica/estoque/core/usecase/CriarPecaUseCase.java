package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.dto.CriarPecaDto;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;

public class CriarPecaUseCase {

    private final PecaGateway pecaGateway;

    public CriarPecaUseCase(PecaGateway pecaGateway) {
        this.pecaGateway = pecaGateway;
    }

    public void criar(CriarPecaDto dto) {
        Peca peca = new Peca(dto.nome(), dto.descricao(), dto.preco(), dto.quantidadeEstoque());
        pecaGateway.criar(peca);
    }
}
