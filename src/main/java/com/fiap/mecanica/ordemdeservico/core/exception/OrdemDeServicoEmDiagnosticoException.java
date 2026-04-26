package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OrdemDeServicoEmDiagnosticoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String MESSAGE = "A ordem de servico ja esta em diagnostico";

    public OrdemDeServicoEmDiagnosticoException() {
        super(STATUS, MESSAGE);
    }
}
