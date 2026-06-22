package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class LinkAprovacaoOrcamentoNaoEncontradoException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Link de aprovação de orçamento não encontrado";

    public LinkAprovacaoOrcamentoNaoEncontradoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
