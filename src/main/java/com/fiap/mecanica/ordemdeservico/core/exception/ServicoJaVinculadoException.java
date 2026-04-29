package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ServicoJaVinculadoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Este serviço já está vinculado à ordem de serviço";

    public ServicoJaVinculadoException() {
        super(STATUS, MESSAGE);
    }
}
