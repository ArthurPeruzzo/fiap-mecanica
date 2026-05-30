package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class ServicoNaoEncontradoException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Serviço não encontrado";

    public ServicoNaoEncontradoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
