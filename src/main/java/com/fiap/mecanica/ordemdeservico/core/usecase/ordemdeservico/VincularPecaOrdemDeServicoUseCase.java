package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VincularPecaOrdemDeServicoUseCase {
	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final PecaGateway pecaGateway;

	public void vincular(Long ordemServicoId, Long pecaId, Integer quantidade) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		Peca peca = pecaGateway.buscarPorId(pecaId).orElseThrow(PecaNaoEncontradaException::new);

		peca.baixarEstoque(quantidade);
		ordemDeServico.vincularPeca(pecaId, quantidade, peca.getPreco());

		pecaGateway.atualizar(peca);
		ordemDeServicoGateway.vincularOuSomarPeca(ordemServicoId, pecaId, quantidade);
	}
}
