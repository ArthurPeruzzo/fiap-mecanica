package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ConcluirDiagnosticoOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.IniciarDiagnosticoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;

public class DiagnosticoOrdemDeServicoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final MecanicoGateway mecanicoGateway;
    private final TokenGateway tokenGateway;

    public DiagnosticoOrdemDeServicoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                                     MecanicoGateway mecanicoGateway,
                                                     TokenGateway tokenGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.mecanicoGateway = mecanicoGateway;
        this.tokenGateway = tokenGateway;
    }

    public void iniciarDiagnostico(Long ordemServicoId) {
        new IniciarDiagnosticoOrdemDeServicoUseCase(mecanicoGateway, tokenGateway, ordemDeServicoGateway)
                .iniciarDiagnostico(ordemServicoId);
    }

    public void concluirDiagnostico(Long ordemServicoId) {
        new ConcluirDiagnosticoOrdemDeServicoUseCase(mecanicoGateway, tokenGateway, ordemDeServicoGateway)
                .concluirDiagnostico(ordemServicoId);
    }
}
