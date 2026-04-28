package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class QuantidadeDesvincularInvalidaException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Quantidade a desvincular é maior que a quantidade vinculada";

    public QuantidadeDesvincularInvalidaException() {
        super(STATUS, MESSAGE);
    }
}
