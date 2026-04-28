package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class DesvincularInsumoNaoAutorizadaException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Não é possível desvincular insumos se a ordem de serviço não está em diagnóstico";

    public DesvincularInsumoNaoAutorizadaException() {
        super(STATUS, MESSAGE);
    }
}
