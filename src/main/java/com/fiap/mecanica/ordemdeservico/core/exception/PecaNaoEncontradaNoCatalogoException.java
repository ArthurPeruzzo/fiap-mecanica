package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class PecaNaoEncontradaNoCatalogoException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Peça não encontrada";

    public PecaNaoEncontradaNoCatalogoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
