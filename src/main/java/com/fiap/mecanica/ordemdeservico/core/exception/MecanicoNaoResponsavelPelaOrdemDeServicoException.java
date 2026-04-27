package com.fiap.mecanica.ordemdeservico.core.exception;

import com.fiap.mecanica.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class MecanicoNaoResponsavelPelaOrdemDeServicoException extends BaseException {

	private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
	private static final String MESSAGE = "Somente o mecânico responsável pelo diagnóstico pode concluí-lo";

	public MecanicoNaoResponsavelPelaOrdemDeServicoException() {
		super(STATUS, MESSAGE);
	}
}