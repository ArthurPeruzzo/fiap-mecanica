package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ServicoNaoVinculadoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Este serviço não está vinculado à ordem de serviço";

    public ServicoNaoVinculadoException() {
        super(STATUS, MESSAGE);
    }
}
