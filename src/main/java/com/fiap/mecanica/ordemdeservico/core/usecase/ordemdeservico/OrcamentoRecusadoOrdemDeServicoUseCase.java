package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrcamentoRecusadoOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final PecaGateway pecaGateway;
	private final InsumoGateway insumoGateway;

	public void recursar(Long ordemDeServicoId) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemDeServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		ordemDeServico.cancelar();
		devolverQuantidadesDePecaAoEstoque(ordemDeServico);
		devolverQuantidadesDeInsumoAoEstoque(ordemDeServico);


		ordemDeServicoGateway.atualizar(ordemDeServico);

	}

	private void devolverQuantidadesDePecaAoEstoque(OrdemDeServico ordemDeServico) {
		Map<Long, Integer> totalQuantidadePecasMapeadasPorId = ordemDeServico.getTotalQuantidadePecasMapeadasPorId();
		List<Peca> pecas = new ArrayList<>();

		totalQuantidadePecasMapeadasPorId.forEach((pecaId, quantidade) -> {
			Peca peca = pecaGateway.buscarPorId(pecaId).orElseThrow(PecaNaoEncontradaException::new);
			peca.devolverEstoque(quantidade);
			pecas.add(peca);
		});

		pecas.forEach(pecaGateway::atualizar);
	}

	private void devolverQuantidadesDeInsumoAoEstoque(OrdemDeServico ordemDeServico) {
		Map<Long, Integer> totalQuantidadeInsumosMapeadosPorId = ordemDeServico.getTotalQuantidadeInsumosMapeadosPorId();
		List<Insumo> insumos = new ArrayList<>();

		totalQuantidadeInsumosMapeadosPorId.forEach((insumoId, quantidade) -> {
			Insumo insumo = insumoGateway.buscarPorId(insumoId).orElseThrow(InsumoNaoEncontradoException::new);
			insumo.devolverEstoque(quantidade);
			insumos.add(insumo);
		});

		insumos.forEach(insumoGateway::atualizar);
	}
}
