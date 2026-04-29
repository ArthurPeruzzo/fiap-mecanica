package com.fiap.mecanica.estoque.infra.controller.json;

import com.fiap.mecanica.estoque.core.domain.Peca;

import java.math.BigDecimal;

public record PecaResponseJson(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque) {

    public static PecaResponseJson from(Peca peca) {
        return new PecaResponseJson(
                peca.getId(),
                peca.getNome(),
                peca.getDescricao(),
                peca.getPreco(),
                peca.getEstoqueTotal()
        );
    }
}
