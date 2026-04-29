package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InsumoNaoVinculadoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Insumo não está vinculado à ordem de serviço";

    public InsumoNaoVinculadoException() {
        super(STATUS, MESSAGE);
    }
}
