package com.fiap.mecanica.estoque.infra.controller.json;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;

import java.math.BigDecimal;

public record InsumoResponseJson(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, UnidadeMedida unidadeMedida) {

    public static InsumoResponseJson from(Insumo insumo) {
        return new InsumoResponseJson(
                insumo.getId(),
                insumo.getNome(),
                insumo.getDescricao(),
                insumo.getPreco(),
                insumo.getEstoqueTotal(),
                insumo.getUnidadeMedida()
        );
    }
}
