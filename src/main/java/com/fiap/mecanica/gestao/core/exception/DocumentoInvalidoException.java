package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class DocumentoInvalidoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String MESSAGE = "O cnpj ou cpf precisam estar preenchidos";

    public DocumentoInvalidoException() {
        super(STATUS, MESSAGE);
    }
}
