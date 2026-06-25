package com.fiap.mecanica.estoque.infra.controller.json;

import java.math.BigDecimal;

public record PecaResponseJson(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque) {}
