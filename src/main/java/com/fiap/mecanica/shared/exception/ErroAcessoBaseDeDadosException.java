package com.fiap.mecanica.shared.exception;

import org.springframework.http.HttpStatus;

public class ErroAcessoBaseDeDadosException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final String MESSAGE = "Erro ao acessar base de dados";

    public ErroAcessoBaseDeDadosException() {
        super(STATUS, MESSAGE);
    }
}
