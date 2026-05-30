package com.fiap.mecanica.gestao.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class DocumentoInvalidoException extends BaseException {

    private static final int STATUS_CODE = 400;
    private static final String MESSAGE = "O cnpj ou cpf precisam estar preenchidos";

    public DocumentoInvalidoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
