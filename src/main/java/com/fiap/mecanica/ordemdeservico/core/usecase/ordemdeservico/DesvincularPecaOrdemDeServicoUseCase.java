package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;

public class DesvincularPecaOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final ReservaEstoqueGateway reservaEstoqueGateway;

	public DesvincularPecaOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
												 ReservaEstoqueGateway reservaEstoqueGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.reservaEstoqueGateway = reservaEstoqueGateway;
	}

	public void desvincular(Long ordemServicoId, Long pecaId, Integer quantidade) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		ordemDeServico.desvincularPeca(pecaId, quantidade);

		reservaEstoqueGateway.reservarPeca(ordemServicoId, pecaId, ordemDeServico.getTotalQuantidadePecasMapeadasPorId().getOrDefault(pecaId, 0));
		ordemDeServicoGateway.desvincularOuSubtrairPeca(ordemServicoId, pecaId, quantidade);
	}
}
