package com.fiap.mecanica.estoque.core.dto;

import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;

import java.math.BigDecimal;

public record AtualizarInsumoDto(Long id, String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque) {
}
