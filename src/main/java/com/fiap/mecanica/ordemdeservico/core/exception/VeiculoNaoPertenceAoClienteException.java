package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class VeiculoNaoPertenceAoClienteException extends BaseException {

	private static final int STATUS_CODE = 422;
	private static final String MESSAGE = "Veículo não pertence ao cliente informado";

	public VeiculoNaoPertenceAoClienteException() {
		super(STATUS_CODE, MESSAGE);
	}
}