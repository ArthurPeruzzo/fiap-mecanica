package com.fiap.mecanica.estoque.infra.controller.json;

import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;

import java.math.BigDecimal;

public record InsumoResponseJson(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, UnidadeMedida unidadeMedida) {}
