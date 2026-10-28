package com.fiap.mecanica.resources.estoque;

import com.fiap.mecanica.estoque.core.domain.TipoItemReserva;
import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.mecanica.estoque.core.gateway.ReservaGateway;
import com.fiap.mecanica.estoque.core.usecase.ConfirmarReservasOrdemDeServicoUseCase;
import com.fiap.mecanica.estoque.core.usecase.LiberarReservasOrdemDeServicoUseCase;
import com.fiap.mecanica.estoque.core.usecase.ReservarItemEstoqueUseCase;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueInsuficienteParaOrdemException;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Substitui o cliente HTTP do fiap-estoque nos testes de integração do monólito: aplica as reservas
 * no módulo de estoque embutido (mesmo banco do teste), sem depender do outro serviço no ar.
 * O contrato HTTP real é coberto por ReservaEstoqueHttpGatewayUnitTest e pelos testes do fiap-estoque.
 */
@Component
@Primary
@Profile("integration-test")
@RequiredArgsConstructor
public class ReservaEstoqueLocalFake implements ReservaEstoqueGateway {

	private final ItemEstoqueGateway itemEstoqueGateway;
	private final ReservaGateway reservaGateway;

	@Override
	public void reservarItens(Long ordemServicoId, List<ItemReserva> itens) {
		try {
			var useCase = new ReservarItemEstoqueUseCase(itemEstoqueGateway, reservaGateway);
			itens.forEach(item -> useCase.reservar(
					ordemServicoId, TipoItemReserva.valueOf(item.tipo().name()), item.itemId(), item.quantidadeTotal()));
		} catch (EstoqueInsuficienteException e) {
			throw new EstoqueInsuficienteParaOrdemException();
		}
	}

	@Override
	public void confirmarReservas(Long ordemServicoId) {
		new ConfirmarReservasOrdemDeServicoUseCase(itemEstoqueGateway, reservaGateway).confirmar(ordemServicoId);
	}

	@Override
	public void liberarReservas(Long ordemServicoId) {
		new LiberarReservasOrdemDeServicoUseCase(itemEstoqueGateway, reservaGateway).liberar(ordemServicoId);
	}
}
