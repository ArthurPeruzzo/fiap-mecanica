package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class DesvincularPecaNaoAutorizadaException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Não é possível desvincular peças se a ordem de serviço não está em diagnóstico";

    public DesvincularPecaNaoAutorizadaException() {
        super(STATUS_CODE, MESSAGE);
    }
}
