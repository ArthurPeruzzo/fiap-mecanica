package com.fiap.mecanica.estoque.core.domain;

import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.core.exception.QuantidadeEstoqueMenorQueReservadaException;

public abstract class ItemEstoque {
	private Integer quantidadeEstoque;
	private Integer quantidadeReservada;

	protected ItemEstoque(Integer quantidadeEstoque) {
		this(quantidadeEstoque, 0);
	}

	protected ItemEstoque(Integer quantidadeEstoque, Integer quantidadeReservada) {
		this.quantidadeEstoque = quantidadeEstoque;
		this.quantidadeReservada = quantidadeReservada == null ? 0 : quantidadeReservada;
	}

	public void reservar(Integer quantidade) {
		if (getEstoqueDisponivel() < quantidade) {
			throw new EstoqueInsuficienteException();
		}
		this.quantidadeReservada += quantidade;
	}

	public void liberarReserva(Integer quantidade) {
		this.quantidadeReservada = Math.max(0, this.quantidadeReservada - quantidade);
	}

	public void confirmarBaixa(Integer quantidade) {
		liberarReserva(quantidade);
		this.quantidadeEstoque -= quantidade;
	}

	protected void ajustarQuantidade(Integer quantidade) {
		if (quantidade < this.quantidadeReservada) {
			throw new QuantidadeEstoqueMenorQueReservadaException();
		}
		this.quantidadeEstoque = quantidade;
	}

	public Integer getEstoqueTotal() {
		return quantidadeEstoque;
	}

	public Integer getQuantidadeReservada() {
		return quantidadeReservada;
	}

	public Integer getEstoqueDisponivel() {
		return quantidadeEstoque - quantidadeReservada;
	}
}
