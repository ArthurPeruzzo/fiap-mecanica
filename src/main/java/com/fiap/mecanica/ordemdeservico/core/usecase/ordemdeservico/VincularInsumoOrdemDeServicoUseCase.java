package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;

public class VincularInsumoOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final InsumoGateway insumoGateway;
    private final ReservaEstoqueGateway reservaEstoqueGateway;

    public VincularInsumoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                                InsumoGateway insumoGateway,
                                                ReservaEstoqueGateway reservaEstoqueGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.insumoGateway = insumoGateway;
        this.reservaEstoqueGateway = reservaEstoqueGateway;
    }

    public void vincular(Long ordemServicoId, Long insumoId, Integer quantidade) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        Insumo insumo = insumoGateway.buscarPorId(insumoId).orElseThrow(InsumoNaoEncontradoException::new);

        ordemDeServico.vincularInsumo(insumoId, quantidade, insumo.getPreco());

        reservaEstoqueGateway.reservarInsumo(ordemServicoId, insumoId, ordemDeServico.getTotalQuantidadeInsumosMapeadosPorId().get(insumoId));
        ordemDeServicoGateway.vincularOuSomarInsumo(ordemServicoId, insumoId, quantidade, insumo.getPreco());
    }
}
