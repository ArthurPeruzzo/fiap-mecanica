package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class TransicaoDeStatusInvalidaException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "A ordem de serviço não está no status correto para esta operação";

    public TransicaoDeStatusInvalidaException() {
        super(STATUS_CODE, MESSAGE);
    }
}
