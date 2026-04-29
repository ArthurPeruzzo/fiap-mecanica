package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class VinculoServicoNaoAutorizadoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico";

    public VinculoServicoNaoAutorizadoException() {
        super(STATUS, MESSAGE);
    }
}
