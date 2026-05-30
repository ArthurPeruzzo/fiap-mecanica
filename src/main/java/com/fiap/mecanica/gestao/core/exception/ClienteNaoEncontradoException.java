package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class ClienteNaoEncontradoException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Cliente não encontrado";

    public ClienteNaoEncontradoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
