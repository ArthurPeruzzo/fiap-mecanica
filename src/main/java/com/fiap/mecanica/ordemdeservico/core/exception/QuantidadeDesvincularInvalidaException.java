package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class QuantidadeDesvincularInvalidaException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Quantidade a desvincular é maior que a quantidade vinculada";

    public QuantidadeDesvincularInvalidaException() {
        super(STATUS_CODE, MESSAGE);
    }
}
