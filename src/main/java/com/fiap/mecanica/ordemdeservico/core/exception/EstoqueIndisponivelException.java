package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class EstoqueIndisponivelException extends BaseException {

    private static final int STATUS_CODE = 503;
    private static final String MESSAGE = "Serviço de estoque indisponível no momento. Tente novamente em instantes";

    public EstoqueIndisponivelException() {
        super(STATUS_CODE, MESSAGE);
    }
}
