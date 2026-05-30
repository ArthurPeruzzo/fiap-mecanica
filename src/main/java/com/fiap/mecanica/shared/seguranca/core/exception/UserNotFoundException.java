package com.fiap.mecanica.shared.seguranca.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class UserNotFoundException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Usuario nao encontrado";

    public UserNotFoundException() {
        super(STATUS_CODE, MESSAGE);
    }
}
