package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class MecanicoNaoEncontradoException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Mecânico não encontrado";

    public MecanicoNaoEncontradoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
