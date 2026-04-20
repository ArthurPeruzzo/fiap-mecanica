package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ClienteJaExisteException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String MESSAGE = "Já existe um cliente cadastrado com o documento informado";

    public ClienteJaExisteException() {
        super(STATUS, MESSAGE);
    }
}
