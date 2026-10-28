package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.exception.PecaNaoEncontradaNoCatalogoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;

import java.util.List;

public class VincularPecaOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final CatalogoEstoqueGateway catalogoEstoqueGateway;
	private final ReservaEstoqueGateway reservaEstoqueGateway;

	public VincularPecaOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
											  CatalogoEstoqueGateway catalogoEstoqueGateway,
										  ReservaEstoqueGateway reservaEstoqueGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.catalogoEstoqueGateway = catalogoEstoqueGateway;
		this.reservaEstoqueGateway = reservaEstoqueGateway;
	}

	public void vincular(Long ordemServicoId, Long pecaId, Integer quantidade) {
		OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
				.orElseThrow(OrdemDeServicoNaoEncontradaException::new);

		ItemCatalogo peca = catalogoEstoqueGateway.buscarPeca(pecaId).orElseThrow(PecaNaoEncontradaNoCatalogoException::new);

		ordemDeServico.vincularPeca(pecaId, quantidade, peca.preco());

		reservaEstoqueGateway.reservarItens(ordemServicoId, List.of(
				ItemReserva.peca(pecaId, ordemDeServico.getTotalQuantidadePecasMapeadasPorId().get(pecaId))));
		ordemDeServicoGateway.vincularOuSomarPeca(ordemServicoId, pecaId, quantidade, peca.preco());
	}
}
