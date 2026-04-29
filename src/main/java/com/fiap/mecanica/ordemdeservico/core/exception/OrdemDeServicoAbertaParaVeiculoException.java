package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OrdemDeServicoAbertaParaVeiculoException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String MESSAGE = "Já existe uma ordem de serviço aberta para este veículo";

    public OrdemDeServicoAbertaParaVeiculoException() {
        super(STATUS, MESSAGE);
    }
}
