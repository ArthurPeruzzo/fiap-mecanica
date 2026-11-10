package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrcamentoAprovadoFactory;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.PagamentoComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

/**
 * Aprovar o orçamento dá a partida na saga de pagamento: a OS vai para AGUARDANDO_PAGAMENTO e o pedido de cobrança é
 * gravado na outbox na mesma transação. A OS só entra em EM_EXECUCAO quando a cobrança e a baixa do estoque terminarem
 * (ver {@link PagamentoConfirmadoOrdemDeServicoUseCase} e {@link BaixaConfirmadaOrdemDeServicoUseCase}).
 */
public abstract class OrcamentoAprovadoOrdemDeServicoUseCase {

	protected final OrdemDeServicoGateway ordemDeServicoGateway;
	private final PagamentoComandosGateway pagamentoComandosGateway;
	private final TransacaoGateway transacaoGateway;
	private final NotificacaoGateway notificacaoGateway;

	protected OrcamentoAprovadoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
													  PagamentoComandosGateway pagamentoComandosGateway,
													  TransacaoGateway transacaoGateway,
													  NotificacaoGateway notificacaoGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.pagamentoComandosGateway = pagamentoComandosGateway;
		this.transacaoGateway = transacaoGateway;
		this.notificacaoGateway = notificacaoGateway;
	}

	public void aprovar(OrdemDeServico ordemDeServico) {
		ordemDeServico.aprovar();
		transacaoGateway.executar(() -> {
			ordemDeServicoGateway.atualizar(ordemDeServico);
			pagamentoComandosGateway.solicitarPagamento(ordemDeServico.getId(), ordemDeServico.getClienteId(),
					ordemDeServico.getValorTotalOrcamento());
		});
		var params = MensagemParams.builder()
				.clienteId(ordemDeServico.getClienteId())
				.ordemId(ordemDeServico.getId())
				.build();
		notificacaoGateway.enviar(new MensagemOrcamentoAprovadoFactory().criar(params));
	}
}
