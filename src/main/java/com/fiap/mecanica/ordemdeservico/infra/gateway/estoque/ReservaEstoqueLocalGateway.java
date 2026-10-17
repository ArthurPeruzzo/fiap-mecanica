package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.fiap.mecanica.estoque.core.domain.TipoItemReserva;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.core.gateway.ReservaGateway;
import com.fiap.mecanica.estoque.core.usecase.ConfirmarReservasOrdemDeServicoUseCase;
import com.fiap.mecanica.estoque.core.usecase.LiberarReservasOrdemDeServicoUseCase;
import com.fiap.mecanica.estoque.core.usecase.ReservarItemEstoqueUseCase;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservaEstoqueLocalGateway implements ReservaEstoqueGateway {

	private final PecaGateway pecaGateway;
	private final InsumoGateway insumoGateway;
	private final ReservaGateway reservaGateway;

	@Override
	public void reservarPeca(Long ordemServicoId, Long pecaId, Integer quantidadeTotal) {
		new ReservarItemEstoqueUseCase(pecaGateway, insumoGateway, reservaGateway)
				.reservar(ordemServicoId, TipoItemReserva.PECA, pecaId, quantidadeTotal);
	}

	@Override
	public void reservarInsumo(Long ordemServicoId, Long insumoId, Integer quantidadeTotal) {
		new ReservarItemEstoqueUseCase(pecaGateway, insumoGateway, reservaGateway)
				.reservar(ordemServicoId, TipoItemReserva.INSUMO, insumoId, quantidadeTotal);
	}

	@Override
	public void confirmarReservas(Long ordemServicoId) {
		new ConfirmarReservasOrdemDeServicoUseCase(pecaGateway, insumoGateway, reservaGateway).confirmar(ordemServicoId);
	}

	@Override
	public void liberarReservas(Long ordemServicoId) {
		new LiberarReservasOrdemDeServicoUseCase(pecaGateway, insumoGateway, reservaGateway).liberar(ordemServicoId);
	}
}
