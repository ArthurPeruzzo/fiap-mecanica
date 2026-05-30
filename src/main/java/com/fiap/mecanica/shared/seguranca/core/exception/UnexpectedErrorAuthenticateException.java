package com.fiap.mecanica.shared.seguranca.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class UnexpectedErrorAuthenticateException extends BaseException {

    private static final int STATUS_CODE = 500;
    private static final String MESSAGE = "Não foi possível realizar a autenticação";

    public UnexpectedErrorAuthenticateException() {
        super(STATUS_CODE, MESSAGE);
    }
}
