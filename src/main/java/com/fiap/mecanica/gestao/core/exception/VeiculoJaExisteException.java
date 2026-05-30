package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class VeiculoJaExisteException extends BaseException {

    private static final int STATUS_CODE = 409;
    private static final String MESSAGE = "Já existe um veículo cadastrado com a placa informada";

    public VeiculoJaExisteException() {
        super(STATUS_CODE, MESSAGE);
    }
}
