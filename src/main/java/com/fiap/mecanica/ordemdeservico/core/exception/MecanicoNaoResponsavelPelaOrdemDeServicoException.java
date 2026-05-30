package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;

public class MecanicoNaoResponsavelPelaOrdemDeServicoException extends BaseException {

	private static final int STATUS_CODE = 422;
	private static final String MESSAGE = "Somente o mecânico responsável pelo diagnóstico pode concluí-lo";

	public MecanicoNaoResponsavelPelaOrdemDeServicoException() {
		super(STATUS_CODE, MESSAGE);
	}
}