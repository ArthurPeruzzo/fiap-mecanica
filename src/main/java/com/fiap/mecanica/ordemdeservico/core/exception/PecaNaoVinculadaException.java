package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PecaNaoVinculadaException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Peça não está vinculada à ordem de serviço";

    public PecaNaoVinculadaException() {
        super(STATUS, MESSAGE);
    }
}
