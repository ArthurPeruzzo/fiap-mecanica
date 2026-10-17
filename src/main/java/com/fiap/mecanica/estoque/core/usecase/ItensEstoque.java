package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.ItemEstoque;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.TipoItemReserva;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;

class ItensEstoque {

	private final PecaGateway pecaGateway;
	private final InsumoGateway insumoGateway;

	ItensEstoque(PecaGateway pecaGateway, InsumoGateway insumoGateway) {
		this.pecaGateway = pecaGateway;
		this.insumoGateway = insumoGateway;
	}

	ItemEstoque buscar(TipoItemReserva tipoItem, Long itemId) {
		return switch (tipoItem) {
			case PECA -> pecaGateway.buscarPorId(itemId).orElseThrow(PecaNaoEncontradaException::new);
			case INSUMO -> insumoGateway.buscarPorId(itemId).orElseThrow(InsumoNaoEncontradoException::new);
		};
	}

	void atualizar(ItemEstoque item) {
		switch (item) {
			case Peca peca -> pecaGateway.atualizar(peca);
			case Insumo insumo -> insumoGateway.atualizar(insumo);
			default -> throw new IllegalArgumentException("Tipo de item de estoque desconhecido");
		}
	}
}
