package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrdemEntregueFactory;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntregarOrdemDeServicoUseCase {
	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final NotificacaoGateway notificacaoGateway;

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
	}
}
