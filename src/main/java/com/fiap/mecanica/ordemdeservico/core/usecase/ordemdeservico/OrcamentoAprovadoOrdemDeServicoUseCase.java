package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrcamentoAprovadoFactory;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

public abstract class OrcamentoAprovadoOrdemDeServicoUseCase {

	protected final OrdemDeServicoGateway ordemDeServicoGateway;
	private final ReservaEstoqueGateway reservaEstoqueGateway;
	private final NotificacaoGateway notificacaoGateway;

	protected OrcamentoAprovadoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
													  ReservaEstoqueGateway reservaEstoqueGateway,
													  NotificacaoGateway notificacaoGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.reservaEstoqueGateway = reservaEstoqueGateway;
		this.notificacaoGateway = notificacaoGateway;
	}

	public void aprovar(OrdemDeServico ordemDeServico) {
		ordemDeServico.aprovar();
		reservaEstoqueGateway.confirmarReservas(ordemDeServico.getId());
		ordemDeServicoGateway.atualizar(ordemDeServico);
		var params = MensagemParams.builder()
				.clienteId(ordemDeServico.getClienteId())
				.ordemId(ordemDeServico.getId())
				.build();
		notificacaoGateway.enviar(new MensagemOrcamentoAprovadoFactory().criar(params));
	}
}
