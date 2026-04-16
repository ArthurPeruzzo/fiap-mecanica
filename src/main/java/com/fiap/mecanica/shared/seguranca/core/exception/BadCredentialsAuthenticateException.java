package com.fiap.mecanica.shared.seguranca.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class BadCredentialsAuthenticateException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    private static final String MESSAGE = "Usuário ou senha incorretos";

    public BadCredentialsAuthenticateException() {
        super(STATUS, MESSAGE);
    }
}
