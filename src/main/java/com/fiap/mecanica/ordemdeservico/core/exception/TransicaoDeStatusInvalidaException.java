package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TransicaoDeStatusInvalidaException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "A ordem de serviço não está no status correto para esta operação";

    public TransicaoDeStatusInvalidaException() {
        super(STATUS, MESSAGE);
    }
}
