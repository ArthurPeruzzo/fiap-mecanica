package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;

/**
 * Saga, último passo (resposta do estoque): pagamento e baixa concluídos, a OS vai para EM_EXECUCAO. Idempotente:
 * mensagem repetida, com a OS já fora de AGUARDANDO_PAGAMENTO, é ignorada.
 */
public class BaixaConfirmadaOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;

	public BaixaConfirmadaOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
	}

	/** @return true se a OS entrou em execução; false se a mensagem foi ignorada por já estar processada. */
	public boolean executar(Long ordemServicoId) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);
		if (!StatusOrdemDeServico.AGUARDANDO_PAGAMENTO.equals(ordemDeServico.getStatus())) {
			return false;
		}
		ordemDeServico.iniciarExecucao();
		ordemDeServicoGateway.atualizar(ordemDeServico);
		return true;
	}
}
