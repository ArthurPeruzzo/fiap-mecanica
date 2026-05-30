package com.fiap.mecanica.shared.seguranca.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class BadCredentialsAuthenticateException extends BaseException {

    private static final int STATUS_CODE = 401;
    private static final String MESSAGE = "Usuário ou senha incorretos";

    public BadCredentialsAuthenticateException() {
        super(STATUS_CODE, MESSAGE);
    }
}
