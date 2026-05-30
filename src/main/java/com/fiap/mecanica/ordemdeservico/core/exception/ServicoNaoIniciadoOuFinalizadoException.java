package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class ServicoNaoIniciadoOuFinalizadoException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Este serviço ainda não foi iniciado ou já foi finalizado";

    public ServicoNaoIniciadoOuFinalizadoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
