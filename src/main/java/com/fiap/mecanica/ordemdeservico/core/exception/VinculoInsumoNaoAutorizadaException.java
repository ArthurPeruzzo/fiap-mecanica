package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class VinculoInsumoNaoAutorizadaException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Não é possível vincular insumos se a ordem de serviço não está em diagnóstico ou recebida";

    public VinculoInsumoNaoAutorizadaException() {
        super(STATUS_CODE, MESSAGE);
    }
}
