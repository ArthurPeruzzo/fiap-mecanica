package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class ServicoEmExecucaoOuFinalizadoException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Este serviço já foi iniciado ou finalizado";

    public ServicoEmExecucaoOuFinalizadoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
