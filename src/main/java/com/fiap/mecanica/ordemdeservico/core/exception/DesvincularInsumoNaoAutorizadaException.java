package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class DesvincularInsumoNaoAutorizadaException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Não é possível desvincular insumos se a ordem de serviço não está em diagnóstico";

    public DesvincularInsumoNaoAutorizadaException() {
        super(STATUS_CODE, MESSAGE);
    }
}
