package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IniciarServicoOrdemDeServicoUseCase {
	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final ServicoGateway servicoGateway;

	public void iniciar(Long ordemDeServicoId, Long servicoId) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemDeServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		servicoGateway.buscarPorId(servicoId).orElseThrow(ServicoNaoEncontradoException::new);

		ordemDeServico.iniciarServico(servicoId);

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
	}
}
