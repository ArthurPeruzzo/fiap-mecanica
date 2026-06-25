package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;

public class IniciarDiagnosticoOrdemDeServicoUseCase {

    private final MecanicoGateway mecanicoGateway;
    private final TokenGateway tokenGateway;
    private final OrdemDeServicoGateway ordemDeServicoGateway;

    public IniciarDiagnosticoOrdemDeServicoUseCase(MecanicoGateway mecanicoGateway,
                                                    TokenGateway tokenGateway,
                                                    OrdemDeServicoGateway ordemDeServicoGateway) {
        this.mecanicoGateway = mecanicoGateway;
        this.tokenGateway = tokenGateway;
        this.ordemDeServicoGateway = ordemDeServicoGateway;
    }

    public void iniciarDiagnostico(Long ordemServicoId) {
        Mecanico mecanico = buscaMecanicoPorUsuarioId();
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        ordemDeServico.iniciarDiagnostico(mecanico.getId());
        ordemDeServicoGateway.atualizar(ordemDeServico);
    }

    private Mecanico buscaMecanicoPorUsuarioId() {
        Long userId = tokenGateway.getUserId();
        return mecanicoGateway.findByUsuarioId(userId).orElseThrow(MecanicoNaoEncontradoException::new);
    }
}
