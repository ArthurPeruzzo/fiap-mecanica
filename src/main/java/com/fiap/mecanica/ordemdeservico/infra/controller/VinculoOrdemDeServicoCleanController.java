package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;

public class VinculoOrdemDeServicoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ServicoGateway servicoGateway;
    private final CatalogoEstoqueGateway catalogoEstoqueGateway;
    private final ReservaEstoqueGateway reservaEstoqueGateway;

    public VinculoOrdemDeServicoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                                 ServicoGateway servicoGateway,
                                                 CatalogoEstoqueGateway catalogoEstoqueGateway,
                                                 ReservaEstoqueGateway reservaEstoqueGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.servicoGateway = servicoGateway;
        this.catalogoEstoqueGateway = catalogoEstoqueGateway;
        this.reservaEstoqueGateway = reservaEstoqueGateway;
    }

    public void vincularServico(Long ordemServicoId, Long servicoId) {
        new VincularServicoOrdemDeServicoUseCase(ordemDeServicoGateway, servicoGateway)
                .vincular(ordemServicoId, servicoId);
    }

    public void desvincularServico(Long ordemServicoId, Long servicoId) {
        new DesvincularServicoOrdemDeServicoUseCase(ordemDeServicoGateway, servicoGateway)
                .desvincular(ordemServicoId, servicoId);
    }

    public void vincularPeca(Long ordemServicoId, Long pecaId, Integer quantidade) {
        new VincularPecaOrdemDeServicoUseCase(ordemDeServicoGateway, catalogoEstoqueGateway, reservaEstoqueGateway)
                .vincular(ordemServicoId, pecaId, quantidade);
    }

    public void desvincularPeca(Long ordemServicoId, Long pecaId, Integer quantidade) {
        new DesvincularPecaOrdemDeServicoUseCase(ordemDeServicoGateway, catalogoEstoqueGateway, reservaEstoqueGateway)
                .desvincular(ordemServicoId, pecaId, quantidade);
    }

    public void vincularInsumo(Long ordemServicoId, Long insumoId, Integer quantidade) {
        new VincularInsumoOrdemDeServicoUseCase(ordemDeServicoGateway, catalogoEstoqueGateway, reservaEstoqueGateway)
                .vincular(ordemServicoId, insumoId, quantidade);
    }

    public void desvincularInsumo(Long ordemServicoId, Long insumoId, Integer quantidade) {
        new DesvincularInsumoOrdemDeServicoUseCase(ordemDeServicoGateway, catalogoEstoqueGateway, reservaEstoqueGateway)
                .desvincular(ordemServicoId, insumoId, quantidade);
    }
}
