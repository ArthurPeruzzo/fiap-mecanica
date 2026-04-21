package com.fiap.mecanica.estoque.core.domain;

import java.math.BigDecimal;

public class Insumo extends ItemEstoque {
	private Long id;
	private String nome;
	private String descricao;
	private BigDecimal preco;
	private UnidadeMedida unidadeMedida;

	protected Insumo(int quantidadeEstoque) {
		super(quantidadeEstoque);
	}
}
