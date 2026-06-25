package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;

public class DesvincularPecaOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final PecaGateway pecaGateway;

	public DesvincularPecaOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
												 PecaGateway pecaGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.pecaGateway = pecaGateway;
	}

	public void desvincular(Long ordemServicoId, Long pecaId, Integer quantidade) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		Peca peca = pecaGateway.buscarPorId(pecaId).orElseThrow(PecaNaoEncontradaException::new);

		peca.devolverEstoque(quantidade);
		ordemDeServico.desvincularPeca(pecaId, quantidade);

		pecaGateway.atualizar(peca);
		ordemDeServicoGateway.desvincularOuSubtrairPeca(ordemServicoId, pecaId, quantidade);
	}
}
