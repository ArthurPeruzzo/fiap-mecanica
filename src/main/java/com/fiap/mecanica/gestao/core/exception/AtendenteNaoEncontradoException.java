package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class AtendenteNaoEncontradoException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Atendente não encontrado";

    public AtendenteNaoEncontradoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
