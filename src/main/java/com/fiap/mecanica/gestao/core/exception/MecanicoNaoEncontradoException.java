package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class MecanicoNaoEncontradoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "Mecânico não encontrado";

    public MecanicoNaoEncontradoException() {
        super(STATUS, MESSAGE);
    }
}
