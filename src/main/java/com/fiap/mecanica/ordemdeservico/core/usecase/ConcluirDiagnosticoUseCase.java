package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcluirDiagnosticoUseCase {

    private final MecanicoGateway mecanicoGateway;
    private final TokenGateway tokenGateway;
    private final OrdemDeServicoGateway ordemDeServicoGateway;

    public void concluirDiagnostico(Long ordemId) {
        verificaMecanico();
        var ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        ordemDeServico.concluirDiagnostico();
        ordemDeServicoGateway.atualizar(ordemDeServico);
    }

    private void verificaMecanico() {
        Long userId = tokenGateway.getUserId();
        mecanicoGateway.findByUsuarioId(userId).orElseThrow(MecanicoNaoEncontradoException::new);
    }
}
