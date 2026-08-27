package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.metricas.core.domain.FaseOrdemDeServico;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;

import java.util.Optional;

public class ConcluirDiagnosticoOrdemDeServicoUseCase {

    private final MecanicoGateway mecanicoGateway;
    private final TokenGateway tokenGateway;
    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final MetricasGateway metricasGateway;

    public ConcluirDiagnosticoOrdemDeServicoUseCase(MecanicoGateway mecanicoGateway,
                                                     TokenGateway tokenGateway,
                                                     OrdemDeServicoGateway ordemDeServicoGateway,
                                                     MetricasGateway metricasGateway) {
        this.mecanicoGateway = mecanicoGateway;
        this.tokenGateway = tokenGateway;
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.metricasGateway = metricasGateway;
    }

    public void concluirDiagnostico(Long ordemServicoId) {
        Mecanico mecanico = buscaMecanico();
        var ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        ordemDeServico.concluirDiagnostico(mecanico.getId());
        ordemDeServicoGateway.atualizar(ordemDeServico);

        Optional.ofNullable(ordemDeServico.getDuracaoDiagnostico())
                .ifPresent(duracao -> metricasGateway.registrarDuracaoFase(FaseOrdemDeServico.DIAGNOSTICO, duracao));
    }

    private Mecanico buscaMecanico() {
        Long userId = tokenGateway.getUserId();
        return mecanicoGateway.findByUsuarioId(userId).orElseThrow(MecanicoNaoEncontradoException::new);
    }
}
