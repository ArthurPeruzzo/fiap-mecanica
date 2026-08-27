package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrdemFinalizadaFactory;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.shared.metricas.core.domain.FaseOrdemDeServico;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;

import java.util.Optional;

public class FinalizarServicoOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final ServicoGateway servicoGateway;
	private final NotificacaoGateway notificacaoGateway;
	private final MetricasGateway metricasGateway;

	public FinalizarServicoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
												  ServicoGateway servicoGateway,
												  NotificacaoGateway notificacaoGateway,
												  MetricasGateway metricasGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.servicoGateway = servicoGateway;
		this.notificacaoGateway = notificacaoGateway;
		this.metricasGateway = metricasGateway;
	}

	public void finalizar(Long ordemDeServicoId, Long servicoId) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemDeServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		servicoGateway.buscarPorId(servicoId).orElseThrow(ServicoNaoEncontradoException::new);

		ordemDeServico.finalizarServico(servicoId);

		var servicoAtualizado = ordemDeServico.getServicosVinculados().stream()
				.filter(s -> s.servicoId().equals(servicoId))
				.findFirst()
				.orElseThrow(ServicoNaoVinculadoException::new);

		ordemDeServicoGateway.atualizarServico(
				ordemDeServicoId,
				servicoId,
				servicoAtualizado.status(),
				servicoAtualizado.dataInicioExecucao(),
				servicoAtualizado.dataFimExecucao()
		);

		if (StatusOrdemDeServico.FINALIZADA.equals(ordemDeServico.getStatus())) {
			ordemDeServicoGateway.atualizar(ordemDeServico);
			var params = MensagemParams.builder()
					.clienteId(ordemDeServico.getClienteId())
					.ordemId(ordemDeServicoId)
					.build();
			notificacaoGateway.enviar(new MensagemOrdemFinalizadaFactory().criar(params));

			Optional.ofNullable(ordemDeServico.getDuracaoExecucao())
					.ifPresent(duracao -> metricasGateway.registrarDuracaoFase(FaseOrdemDeServico.EXECUCAO, duracao));
		}
	}
}
