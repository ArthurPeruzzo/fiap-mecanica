package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrdemCanceladaFactory;
import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public abstract class OrcamentoRecusadoOrdemDeServicoUseCase {

	protected final OrdemDeServicoGateway ordemDeServicoGateway;
	private final EstoqueComandosGateway estoqueComandosGateway;
	private final TransacaoGateway transacaoGateway;
	private final NotificacaoGateway notificacaoGateway;

	protected OrcamentoRecusadoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
													  EstoqueComandosGateway estoqueComandosGateway,
												  TransacaoGateway transacaoGateway,
													  NotificacaoGateway notificacaoGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.estoqueComandosGateway = estoqueComandosGateway;
		this.transacaoGateway = transacaoGateway;
		this.notificacaoGateway = notificacaoGateway;
	}

	/** Transição de cancelamento; a recusa do pagamento usa outra (ver {@link PagamentoRecusadoOrdemDeServicoUseCase}). */
	protected void cancelar(OrdemDeServico ordemDeServico) {
		ordemDeServico.cancelar();
	}

	protected void recusar(OrdemDeServico ordemDeServico) {
		cancelar(ordemDeServico);
		// O cancelamento não depende do estoque estar no ar: o comando de liberação fica na outbox, na mesma
		// transação que grava a OS, e o relay o entrega ao estoque (idempotente) quando houver broker.
		transacaoGateway.executar(() -> {
			ordemDeServicoGateway.atualizar(ordemDeServico);
			estoqueComandosGateway.solicitarLiberacaoDeReservas(ordemDeServico.getId());
		});
		var params = MensagemParams.builder()
				.clienteId(ordemDeServico.getClienteId())
				.ordemId(ordemDeServico.getId())
				.build();
		notificacaoGateway.enviar(new MensagemOrdemCanceladaFactory().criar(params));
	}
}
