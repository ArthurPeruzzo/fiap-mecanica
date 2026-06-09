package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class VinculoPecaNaoAutorizadaException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Não é possível vincular peças se a ordem de serviço não está em diagnóstico ou recebida";

    public VinculoPecaNaoAutorizadaException() {
        super(STATUS_CODE, MESSAGE);
    }
}
