package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ConcluirDiagnosticoOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.IniciarDiagnosticoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;

public class DiagnosticoOrdemDeServicoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final MecanicoGateway mecanicoGateway;
    private final TokenGateway tokenGateway;
    private final MetricasGateway metricasGateway;

    public DiagnosticoOrdemDeServicoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                                     MecanicoGateway mecanicoGateway,
                                                     TokenGateway tokenGateway,
                                                     MetricasGateway metricasGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.mecanicoGateway = mecanicoGateway;
        this.tokenGateway = tokenGateway;
        this.metricasGateway = metricasGateway;
    }

    public void iniciarDiagnostico(Long ordemServicoId) {
        new IniciarDiagnosticoOrdemDeServicoUseCase(mecanicoGateway, tokenGateway, ordemDeServicoGateway)
                .iniciarDiagnostico(ordemServicoId);
    }

    public void concluirDiagnostico(Long ordemServicoId) {
        new ConcluirDiagnosticoOrdemDeServicoUseCase(mecanicoGateway, tokenGateway, ordemDeServicoGateway, metricasGateway)
                .concluirDiagnostico(ordemServicoId);
    }
}
