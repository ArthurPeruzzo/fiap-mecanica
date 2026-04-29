package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OrdemDeServicoNaoEncontradaException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "Ordem de serviço não encontrada";

    public OrdemDeServicoNaoEncontradaException() {
        super(STATUS, MESSAGE);
    }
}
