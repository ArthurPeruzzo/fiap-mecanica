package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class OrdemDeServicoSemServicosException extends BaseException {

	private static final int STATUS_CODE = 422;
	private static final String MESSAGE = "Não é possível concluir o diagnóstico sem ao menos um serviço vinculado";

	public OrdemDeServicoSemServicosException() {
		super(STATUS_CODE, MESSAGE);
	}
}