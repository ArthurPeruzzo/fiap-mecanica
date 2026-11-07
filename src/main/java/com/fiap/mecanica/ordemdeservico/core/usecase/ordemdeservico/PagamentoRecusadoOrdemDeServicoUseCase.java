package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

/**
 * Saga, compensação "pagamento recusado" (resposta da cobrança): cancela a OS e pede ao estoque que libere as reservas
 * (mesmo caminho da recusa do orçamento). Como a baixa só acontece depois do pagamento, não há nada a estornar.
 * Idempotente: com a OS fora de AGUARDANDO_PAGAMENTO a mensagem é ignorada.
 */
public class PagamentoRecusadoOrdemDeServicoUseCase extends OrcamentoRecusadoOrdemDeServicoUseCase {

	public PagamentoRecusadoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
												  EstoqueComandosGateway estoqueComandosGateway,
												  TransacaoGateway transacaoGateway,
												  NotificacaoGateway notificacaoGateway) {
		super(ordemDeServicoGateway, estoqueComandosGateway, transacaoGateway, notificacaoGateway);
	}

	@Override
	protected void cancelar(OrdemDeServico ordemDeServico) {
		ordemDeServico.cancelarPorPagamentoRecusado();
	}

	/** @return true se a OS foi cancelada; false se a mensagem foi ignorada por já estar processada. */
	public boolean executar(Long ordemServicoId) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);
		if (!StatusOrdemDeServico.AGUARDANDO_PAGAMENTO.equals(ordemDeServico.getStatus())) {
			return false;
		}
		recusar(ordemDeServico);
		return true;
	}
}
