package com.fiap.mecanica.shared.seguranca.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class UnexpectedErrorAuthenticateException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final String MESSAGE = "Não foi possível realizar a autenticação";

    public UnexpectedErrorAuthenticateException() {
        super(STATUS, MESSAGE);
    }
}
