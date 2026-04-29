package com.fiap.mecanica.estoque.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class EstoqueInsuficienteException extends BaseException {
	private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
	private static final String MESSAGE = "Estoque insuficiente para realizar a operação";

	public EstoqueInsuficienteException() {
		super(STATUS, MESSAGE);
	}
}
