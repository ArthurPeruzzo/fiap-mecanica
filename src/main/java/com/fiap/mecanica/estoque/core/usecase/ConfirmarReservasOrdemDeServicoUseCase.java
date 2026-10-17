package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.ItemEstoque;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.core.gateway.ReservaGateway;

/**
 * Converte as reservas ativas da ordem de serviço em baixa definitiva do estoque.
 */
public class ConfirmarReservasOrdemDeServicoUseCase {

	private final ItensEstoque itensEstoque;
	private final ReservaGateway reservaGateway;

	public ConfirmarReservasOrdemDeServicoUseCase(PecaGateway pecaGateway, InsumoGateway insumoGateway, ReservaGateway reservaGateway) {
		this.itensEstoque = new ItensEstoque(pecaGateway, insumoGateway);
		this.reservaGateway = reservaGateway;
	}

	public void confirmar(Long ordemServicoId) {
		reservaGateway.listarAtivasPorOrdemDeServico(ordemServicoId).forEach(reserva -> {
			ItemEstoque item = itensEstoque.buscar(reserva.getTipoItem(), reserva.getItemId());
			item.confirmarBaixa(reserva.getQuantidade());
			reserva.confirmar();
			itensEstoque.atualizar(item);
			reservaGateway.salvar(reserva);
		});
	}
}
