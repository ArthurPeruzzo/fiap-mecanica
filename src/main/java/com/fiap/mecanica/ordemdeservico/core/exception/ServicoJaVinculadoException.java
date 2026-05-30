package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class ServicoJaVinculadoException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Este serviço já está vinculado à ordem de serviço";

    public ServicoJaVinculadoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
