package com.fiap.mecanica.estoque.core.domain;

import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;

public abstract class ItemEstoque {
	private Integer quantidadeEstoque;

	protected ItemEstoque(Integer quantidadeEstoque) {
		this.quantidadeEstoque = quantidadeEstoque;
	}

	public void baixarEstoque(Integer quantidade) {
		if (!temEstoqueDisponivel(quantidade)) {
			throw new EstoqueInsuficienteException();
		}
		this.quantidadeEstoque -= quantidade;
	}

	public void devolverEstoque(Integer quantidade) {
		this.quantidadeEstoque += quantidade;
	}

	private boolean temEstoqueDisponivel(Integer quantidade) {
		return this.quantidadeEstoque >= quantidade;
	}

	public Integer getEstoqueTotal() {
		return quantidadeEstoque;
	}
}
