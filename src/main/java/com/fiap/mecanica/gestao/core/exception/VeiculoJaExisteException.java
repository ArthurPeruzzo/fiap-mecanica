package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class VeiculoJaExisteException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String MESSAGE = "Já existe um veículo cadastrado com a placa informada";

    public VeiculoJaExisteException() {
        super(STATUS, MESSAGE);
    }
}
