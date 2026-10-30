package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoEncontradoNoCatalogoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;


public class DesvincularInsumoOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final CatalogoEstoqueGateway catalogoEstoqueGateway;
	private final ReservaEstoqueGateway reservaEstoqueGateway;

	public DesvincularInsumoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
												   CatalogoEstoqueGateway catalogoEstoqueGateway,
												   ReservaEstoqueGateway reservaEstoqueGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.catalogoEstoqueGateway = catalogoEstoqueGateway;
		this.reservaEstoqueGateway = reservaEstoqueGateway;
	}

	public void desvincular(Long ordemServicoId, Long insumoId, Integer quantidade) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		catalogoEstoqueGateway.buscarInsumo(insumoId).orElseThrow(InsumoNaoEncontradoNoCatalogoException::new);

		Integer totalAnterior = ordemDeServico.getTotalQuantidadeInsumosMapeadosPorId().getOrDefault(insumoId, 0);

		ordemDeServico.desvincularInsumo(insumoId, quantidade);

		ReservaComCompensacao.executar(reservaEstoqueGateway, ordemServicoId,
				ItemReserva.insumo(insumoId, ordemDeServico.getTotalQuantidadeInsumosMapeadosPorId().getOrDefault(insumoId, 0)),
				ItemReserva.insumo(insumoId, totalAnterior),
				() -> ordemDeServicoGateway.desvincularOuSubtrairInsumo(ordemServicoId, insumoId, quantidade));
	}
}
