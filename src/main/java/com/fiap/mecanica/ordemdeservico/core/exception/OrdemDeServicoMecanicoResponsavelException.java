package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OrdemDeServicoMecanicoResponsavelException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Já existe um mecanico responsavel pela ordem de serviço";

    public OrdemDeServicoMecanicoResponsavelException() {
        super(STATUS, MESSAGE);
    }
}
