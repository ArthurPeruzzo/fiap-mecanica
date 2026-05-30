package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class InsumoNaoVinculadoException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Insumo não está vinculado à ordem de serviço";

    public InsumoNaoVinculadoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
