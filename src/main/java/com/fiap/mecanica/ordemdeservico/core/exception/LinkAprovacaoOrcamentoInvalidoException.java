package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class LinkAprovacaoOrcamentoInvalidoException extends BaseException {

    private static final int STATUS_CODE = 410;
    private static final String MESSAGE = "Link de aprovação de orçamento expirado ou já utilizado";

    public LinkAprovacaoOrcamentoInvalidoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
