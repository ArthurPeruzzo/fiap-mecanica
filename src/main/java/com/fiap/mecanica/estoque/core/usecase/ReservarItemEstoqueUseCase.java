package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.ItemEstoque;
import com.fiap.mecanica.estoque.core.domain.Reserva;
import com.fiap.mecanica.estoque.core.domain.TipoItemReserva;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.core.gateway.ReservaGateway;

/**
 * Define a quantidade total reservada de um item para uma ordem de serviço.
 * Idempotente: repetir a chamada com o mesmo total não altera o estoque.
 */
public class ReservarItemEstoqueUseCase {

	private final ItensEstoque itensEstoque;
	private final ReservaGateway reservaGateway;

	public ReservarItemEstoqueUseCase(PecaGateway pecaGateway, InsumoGateway insumoGateway, ReservaGateway reservaGateway) {
		this.itensEstoque = new ItensEstoque(pecaGateway, insumoGateway);
		this.reservaGateway = reservaGateway;
	}

	public void reservar(Long ordemServicoId, TipoItemReserva tipoItem, Long itemId, Integer quantidadeTotal) {
		ItemEstoque item = itensEstoque.buscar(tipoItem, itemId);
		Reserva reserva = reservaGateway.buscar(ordemServicoId, tipoItem, itemId)
				.orElseGet(() -> new Reserva(ordemServicoId, tipoItem, itemId));

		int diferenca = quantidadeTotal - reserva.quantidadeAtiva();
		if (diferenca == 0) return;

		reserva.definirQuantidade(quantidadeTotal);
		if (diferenca > 0) {
			item.reservar(diferenca);
		} else if (diferenca < 0) {
			item.liberarReserva(-diferenca);
		}

		itensEstoque.atualizar(item);
		reservaGateway.salvar(reserva);
	}
}
