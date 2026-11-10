package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;

/**
 * Saga, passo "pagamento aprovado" (resposta da cobrança): manda o estoque transformar as reservas em baixa. A OS
 * continua em AGUARDANDO_PAGAMENTO até a resposta do estoque. Idempotente: mensagem repetida ou atrasada, com a OS já
 * fora de AGUARDANDO_PAGAMENTO, é ignorada (o comando de baixa também é idempotente no estoque).
 */
public class PagamentoConfirmadoOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final EstoqueComandosGateway estoqueComandosGateway;

	public PagamentoConfirmadoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
													EstoqueComandosGateway estoqueComandosGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.estoqueComandosGateway = estoqueComandosGateway;
	}

	/** @return true se o comando de baixa foi registrado; false se a mensagem foi ignorada por já estar processada. */
	public boolean executar(Long ordemServicoId) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);
		if (!StatusOrdemDeServico.AGUARDANDO_PAGAMENTO.equals(ordemDeServico.getStatus())) {
			return false;
		}
		estoqueComandosGateway.solicitarConfirmacaoDeBaixa(ordemServicoId);
		return true;
	}
}
