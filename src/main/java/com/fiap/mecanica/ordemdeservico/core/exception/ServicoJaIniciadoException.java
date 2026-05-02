package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ServicoJaIniciadoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Este serviço já foi iniciado ou finalizado";

    public ServicoJaIniciadoException() {
        super(STATUS, MESSAGE);
    }
}
