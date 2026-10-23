package com.fiap.mecanica.estoque.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class ReservaJaConfirmadaException extends BaseException {
	private static final int STATUS_CODE = 422;
	private static final String MESSAGE = "A reserva já foi confirmada e não pode ser alterada";

	public ReservaJaConfirmadaException() {
		super(STATUS_CODE, MESSAGE);
	}
}
