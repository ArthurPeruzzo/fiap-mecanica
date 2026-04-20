package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ClienteNaoEncontradoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "Cliente não encontrado";

    public ClienteNaoEncontradoException() {
        super(STATUS, MESSAGE);
    }
}
