package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;

public class DesvincularInsumoOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final InsumoGateway insumoGateway;
	private final ReservaEstoqueGateway reservaEstoqueGateway;

	public DesvincularInsumoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
												   InsumoGateway insumoGateway,
												   ReservaEstoqueGateway reservaEstoqueGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.insumoGateway = insumoGateway;
		this.reservaEstoqueGateway = reservaEstoqueGateway;
	}

	public void desvincular(Long ordemServicoId, Long insumoId, Integer quantidade) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		insumoGateway.buscarPorId(insumoId).orElseThrow(InsumoNaoEncontradoException::new);

		ordemDeServico.desvincularInsumo(insumoId, quantidade);

		reservaEstoqueGateway.reservarInsumo(ordemServicoId, insumoId, ordemDeServico.getTotalQuantidadeInsumosMapeadosPorId().getOrDefault(insumoId, 0));
		ordemDeServicoGateway.desvincularOuSubtrairInsumo(ordemServicoId, insumoId, quantidade);
	}
}
