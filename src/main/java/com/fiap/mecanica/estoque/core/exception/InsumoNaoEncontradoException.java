package com.fiap.mecanica.estoque.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InsumoNaoEncontradoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "Insumo não encontrado";

    public InsumoNaoEncontradoException() {
        super(STATUS, MESSAGE);
    }
}
