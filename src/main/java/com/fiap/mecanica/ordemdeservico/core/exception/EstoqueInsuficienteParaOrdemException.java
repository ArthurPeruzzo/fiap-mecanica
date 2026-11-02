package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class EstoqueInsuficienteParaOrdemException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Estoque insuficiente para realizar a operação";

    public EstoqueInsuficienteParaOrdemException() {
        super(STATUS_CODE, MESSAGE);
    }
}
