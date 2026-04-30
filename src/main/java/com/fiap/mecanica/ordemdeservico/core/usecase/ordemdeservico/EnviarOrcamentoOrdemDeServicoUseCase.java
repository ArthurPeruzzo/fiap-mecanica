package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnviarOrcamentoOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final NotificacaoGateway notificacaoGateway;

    public void enviar(Long ordemServicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        ordemDeServico.gravarEnvioOrcamento();
        ordemDeServicoGateway.atualizar(ordemDeServico);
        notificacaoGateway.enviarOrcamento(ordemDeServico.getClienteId(), ordemDeServico.getOrcamento().valorTotal());
    }
}
