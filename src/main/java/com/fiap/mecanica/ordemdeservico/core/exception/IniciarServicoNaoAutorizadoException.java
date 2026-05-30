package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class IniciarServicoNaoAutorizadoException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Não é possível iniciar um serviço se a ordem de serviço não está em execução";

    public IniciarServicoNaoAutorizadoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
