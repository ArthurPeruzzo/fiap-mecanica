package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcluirDiagnosticoOrdemDeServicoUseCase {

    private final MecanicoGateway mecanicoGateway;
    private final TokenGateway tokenGateway;
    private final OrdemDeServicoGateway ordemDeServicoGateway;

    public void concluirDiagnostico(Long ordemServicoId) {
        Mecanico mecanico = buscaMecanico();
        var ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        ordemDeServico.concluirDiagnostico(mecanico.getId());
        ordemDeServicoGateway.atualizar(ordemDeServico);
    }

    private Mecanico buscaMecanico() {
        Long userId = tokenGateway.getUserId();
        return mecanicoGateway.findByUsuarioId(userId).orElseThrow(MecanicoNaoEncontradoException::new);
    }
}
