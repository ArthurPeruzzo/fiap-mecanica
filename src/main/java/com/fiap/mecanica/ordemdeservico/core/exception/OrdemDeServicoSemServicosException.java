package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OrdemDeServicoSemServicosException extends BaseException {

	private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
	private static final String MESSAGE = "Não é possível concluir o diagnóstico sem ao menos um serviço vinculado";

	public OrdemDeServicoSemServicosException() {
		super(STATUS, MESSAGE);
	}
}