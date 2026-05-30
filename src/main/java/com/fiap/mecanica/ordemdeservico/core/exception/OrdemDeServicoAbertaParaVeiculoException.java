package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class OrdemDeServicoAbertaParaVeiculoException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Já existe uma ordem de serviço aberta para este veículo";

    public OrdemDeServicoAbertaParaVeiculoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
