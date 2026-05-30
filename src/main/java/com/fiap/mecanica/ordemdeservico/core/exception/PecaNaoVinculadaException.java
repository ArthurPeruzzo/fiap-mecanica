package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class PecaNaoVinculadaException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Peça não está vinculada à ordem de serviço";

    public PecaNaoVinculadaException() {
        super(STATUS_CODE, MESSAGE);
    }
}
