package com.fiap.mecanica.estoque.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PecaNaoEncontradaException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "Peça não encontrada";

    public PecaNaoEncontradaException() {
        super(STATUS, MESSAGE);
    }
}
