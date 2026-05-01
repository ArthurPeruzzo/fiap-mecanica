package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;

public interface OrdemDeServicoState {

    default void iniciarDiagnostico(OrdemDeServico ordemDeServico, Long mecanicoId) {
        throw new TransicaoDeStatusInvalidaException();
    }

    default void concluirDiagnostico(OrdemDeServico ordemDeServico) {
        throw new TransicaoDeStatusInvalidaException();
    }

    default void enviarOrcamento(OrdemDeServico ordemDeServico) {
        throw new TransicaoDeStatusInvalidaException();
    }

    default void cancelar(OrdemDeServico ordemDeServico) {
        throw new TransicaoDeStatusInvalidaException();
    }
}
