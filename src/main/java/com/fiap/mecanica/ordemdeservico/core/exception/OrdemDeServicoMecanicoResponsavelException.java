package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class OrdemDeServicoMecanicoResponsavelException extends BaseException {

    private static final int STATUS_CODE = 422;
    private static final String MESSAGE = "Já existe um mecanico responsavel pela ordem de serviço";

    public OrdemDeServicoMecanicoResponsavelException() {
        super(STATUS_CODE, MESSAGE);
    }
}
