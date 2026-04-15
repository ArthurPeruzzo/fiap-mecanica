package com.fiap.mecanica.shared.seguranca.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "Usuario nao encontrado";

    public UserNotFoundException() {
        super(STATUS, MESSAGE);
    }
}
