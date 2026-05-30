package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class ClienteJaExisteException extends BaseException {

    private static final int STATUS_CODE = 409;
    private static final String MESSAGE = "Já existe um cliente cadastrado com o documento informado";

    public ClienteJaExisteException() {
        super(STATUS_CODE, MESSAGE);
    }
}
