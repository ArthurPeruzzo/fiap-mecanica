package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;

public class VinculoOrdemDeServicoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ServicoGateway servicoGateway;
    private final PecaGateway pecaGateway;
    private final InsumoGateway insumoGateway;

    public VinculoOrdemDeServicoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                                 ServicoGateway servicoGateway,
                                                 PecaGateway pecaGateway,
                                                 InsumoGateway insumoGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.servicoGateway = servicoGateway;
        this.pecaGateway = pecaGateway;
        this.insumoGateway = insumoGateway;
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
        new VincularPecaOrdemDeServicoUseCase(ordemDeServicoGateway, pecaGateway)
                .vincular(ordemServicoId, pecaId, quantidade);
    }

    public void desvincularPeca(Long ordemServicoId, Long pecaId, Integer quantidade) {
        new DesvincularPecaOrdemDeServicoUseCase(ordemDeServicoGateway, pecaGateway)
                .desvincular(ordemServicoId, pecaId, quantidade);
    }

    public void vincularInsumo(Long ordemServicoId, Long insumoId, Integer quantidade) {
        new VincularInsumoOrdemDeServicoUseCase(ordemDeServicoGateway, insumoGateway)
                .vincular(ordemServicoId, insumoId, quantidade);
    }

    public void desvincularInsumo(Long ordemServicoId, Long insumoId, Integer quantidade) {
        new DesvincularInsumoOrdemDeServicoUseCase(ordemDeServicoGateway, insumoGateway)
                .desvincular(ordemServicoId, insumoId, quantidade);
    }
}
