package com.fiap.mecanica.estoque.core.domain;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Peca extends ItemEstoque {
	private Long id;
	private String nome;
	private String descricao;
	private BigDecimal preco;

	public Peca(String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque) {
		super(quantidadeEstoque);
		this.nome = nome;
		this.descricao = descricao;
		this.preco = preco;
	}

	public static Peca reconstituir(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque) {
		var peca = new Peca(nome, descricao, preco, quantidadeEstoque);
		peca.id = id;
		return peca;
	}
}
