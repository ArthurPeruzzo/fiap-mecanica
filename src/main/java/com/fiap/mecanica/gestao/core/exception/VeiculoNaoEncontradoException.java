package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class VeiculoNaoEncontradoException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Veículo não encontrado";

    public VeiculoNaoEncontradoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
