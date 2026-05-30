package com.fiap.mecanica.estoque.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class EstoqueInsuficienteException extends BaseException {
	private static final int STATUS_CODE = 422;
	private static final String MESSAGE = "Estoque insuficiente para realizar a operação";

	public EstoqueInsuficienteException() {
		super(STATUS_CODE, MESSAGE);
	}
}
