package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.ItemEstoque;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.core.gateway.ReservaGateway;

/**
 * Devolve ao saldo disponível tudo que estava reservado para a ordem de serviço.
 */
public class LiberarReservasOrdemDeServicoUseCase {

	private final ItensEstoque itensEstoque;
	private final ReservaGateway reservaGateway;

	public LiberarReservasOrdemDeServicoUseCase(PecaGateway pecaGateway, InsumoGateway insumoGateway, ReservaGateway reservaGateway) {
		this.itensEstoque = new ItensEstoque(pecaGateway, insumoGateway);
		this.reservaGateway = reservaGateway;
	}

	public void liberar(Long ordemServicoId) {
		reservaGateway.listarAtivasPorOrdemDeServico(ordemServicoId).forEach(reserva -> {
			ItemEstoque item = itensEstoque.buscar(reserva.getTipoItem(), reserva.getItemId());
			item.liberarReserva(reserva.getQuantidade());
			reserva.liberar();
			itensEstoque.atualizar(item);
			reservaGateway.salvar(reserva);
		});
	}
}
