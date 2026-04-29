package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class VeiculoNaoPertenceAoClienteException extends BaseException {

	private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
	private static final String MESSAGE = "Veículo não pertence ao cliente informado";

	public VeiculoNaoPertenceAoClienteException() {
		super(STATUS, MESSAGE);
	}
}