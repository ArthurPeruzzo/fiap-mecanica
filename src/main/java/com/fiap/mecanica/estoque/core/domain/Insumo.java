package com.fiap.mecanica.estoque.core.domain;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Insumo extends ItemEstoque {
	private Long id;
	private String nome;
	private String descricao;
	private BigDecimal preco;
	private UnidadeMedida unidadeMedida;

	public Insumo(String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque) {
		super(quantidadeEstoque);
		this.nome = nome;
		this.descricao = descricao;
		this.preco = preco;
		this.unidadeMedida = unidadeMedida;
	}

	public static Insumo reconstituir(Long id, String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque) {
		var insumo = new Insumo(nome, descricao, preco, unidadeMedida, quantidadeEstoque);
		insumo.id = id;
		return insumo;
	}
}
