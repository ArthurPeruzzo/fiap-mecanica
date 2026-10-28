package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoEncontradoNoCatalogoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;

import java.util.List;

public class VincularInsumoOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final CatalogoEstoqueGateway catalogoEstoqueGateway;
    private final ReservaEstoqueGateway reservaEstoqueGateway;

    public VincularInsumoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                                CatalogoEstoqueGateway catalogoEstoqueGateway,
                                                ReservaEstoqueGateway reservaEstoqueGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.catalogoEstoqueGateway = catalogoEstoqueGateway;
        this.reservaEstoqueGateway = reservaEstoqueGateway;
    }

    public void vincular(Long ordemServicoId, Long insumoId, Integer quantidade) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        ItemCatalogo insumo = catalogoEstoqueGateway.buscarInsumo(insumoId).orElseThrow(InsumoNaoEncontradoNoCatalogoException::new);

        ordemDeServico.vincularInsumo(insumoId, quantidade, insumo.preco());

        reservaEstoqueGateway.reservarItens(ordemServicoId, List.of(
				ItemReserva.insumo(insumoId, ordemDeServico.getTotalQuantidadeInsumosMapeadosPorId().get(insumoId))));
        ordemDeServicoGateway.vincularOuSomarInsumo(ordemServicoId, insumoId, quantidade, insumo.preco());
    }
}
