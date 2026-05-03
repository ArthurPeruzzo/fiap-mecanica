package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ServicoNaoIniciadoOuFinalizadoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Este serviço ainda não foi iniciado ou já foi finalizado";

    public ServicoNaoIniciadoOuFinalizadoException() {
        super(STATUS, MESSAGE);
    }
}
