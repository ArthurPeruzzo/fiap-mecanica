package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoEmDiagnosticoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoMecanicoResponsavelException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IniciarDiagnosticoOrdemDeServicoUseCase {

    private final MecanicoGateway mecanicoGateway;
    private final TokenGateway tokenGateway;
    private final OrdemDeServicoGateway ordemDeServicoGateway;

    public void iniciarDiagnostico(Long ordemId) {
        Mecanico mecanico = buscaMecanicoPorUsuarioId();
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        if (StatusOrdemDeServico.EM_DIAGNOSTICO.equals(ordemDeServico.getStatus())) {
            throw new OrdemDeServicoEmDiagnosticoException();
        }

        if (ordemDeServico.possuiMecanicoResponsavel() && !ordemDeServico.isMecanicoResponsavel(mecanico.getId())) {
            throw new OrdemDeServicoMecanicoResponsavelException();
        }

        ordemDeServico.iniciarDiagnostico(mecanico.getId());
        ordemDeServicoGateway.atualizar(ordemDeServico);
    }

    private Mecanico buscaMecanicoPorUsuarioId() {
        Long userId = tokenGateway.getUserId();
        return mecanicoGateway.findByUsuarioId(userId).orElseThrow(MecanicoNaoEncontradoException::new);
    }
}
