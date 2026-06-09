package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class VinculoServicoNaoAutorizadoException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico ou recebida";

    public VinculoServicoNaoAutorizadoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
