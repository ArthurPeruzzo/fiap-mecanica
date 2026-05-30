package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class OrdemDeServicoNaoEncontradaException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Ordem de serviço não encontrada";

    public OrdemDeServicoNaoEncontradaException() {
        super(STATUS_CODE, MESSAGE);
    }
}
