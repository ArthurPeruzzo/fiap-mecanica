package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;

public abstract class OrcamentoAprovadoOrdemDeServicoUseCase {

	protected final OrdemDeServicoGateway ordemDeServicoGateway;

	protected OrcamentoAprovadoOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
	}

	public void aprovar(OrdemDeServico ordemDeServico) {
		ordemDeServico.aprovar();
		ordemDeServicoGateway.atualizar(ordemDeServico);
	}
}
