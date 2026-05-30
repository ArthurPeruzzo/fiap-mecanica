package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class FinalizarServicoNaoAutorizadoException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Não é possível finalizar um serviço se a ordem de serviço não está em execução";

    public FinalizarServicoNaoAutorizadoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
