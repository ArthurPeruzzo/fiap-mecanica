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

	public void atualizar(String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque) {
		this.nome = nome;
		this.descricao = descricao;
		this.preco = preco;
		this.unidadeMedida = unidadeMedida;
		ajustarQuantidade(quantidadeEstoque);
	}

	private Insumo(String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque, Integer quantidadeReservada) {
		super(quantidadeEstoque, quantidadeReservada);
		this.nome = nome;
		this.descricao = descricao;
		this.preco = preco;
		this.unidadeMedida = unidadeMedida;
	}

	public static Insumo reconstituir(Long id, String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque) {
		return reconstituir(id, nome, descricao, preco, unidadeMedida, quantidadeEstoque, 0);
	}

	public static Insumo reconstituir(Long id, String nome, String descricao, BigDecimal preco, UnidadeMedida unidadeMedida, Integer quantidadeEstoque, Integer quantidadeReservada) {
		var insumo = new Insumo(nome, descricao, preco, unidadeMedida, quantidadeEstoque, quantidadeReservada);
		insumo.id = id;
		return insumo;
	}
}
