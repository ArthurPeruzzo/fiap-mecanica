package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrdemEntregueFactory;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.metricas.core.domain.FaseOrdemDeServico;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

import java.util.Optional;

public class EntregarOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final NotificacaoGateway notificacaoGateway;
	private final MetricasGateway metricasGateway;

	public EntregarOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
										  NotificacaoGateway notificacaoGateway,
										  MetricasGateway metricasGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.notificacaoGateway = notificacaoGateway;
		this.metricasGateway = metricasGateway;
	}

	public void entregar(Long ordemServicoId) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		ordemDeServico.entregar();
		ordemDeServicoGateway.atualizar(ordemDeServico);

		var params = MensagemParams.builder()
				.clienteId(ordemDeServico.getClienteId())
				.ordemId(ordemServicoId)
				.build();
		notificacaoGateway.enviar(new MensagemOrdemEntregueFactory().criar(params));

		Optional.ofNullable(ordemDeServico.getDuracaoEntrega())
				.ifPresent(duracao -> metricasGateway.registrarDuracaoFase(FaseOrdemDeServico.ENTREGA, duracao));
	}
}
