package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class AtendenteNaoEncontradoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "Atendente não encontrado";

    public AtendenteNaoEncontradoException() {
        super(STATUS, MESSAGE);
    }
}
