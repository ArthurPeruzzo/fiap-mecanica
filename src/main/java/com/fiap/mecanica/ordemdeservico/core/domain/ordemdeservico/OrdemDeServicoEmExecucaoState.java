package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

class OrdemDeServicoEmExecucaoState implements OrdemDeServicoState {

	@Override
	public void finalizar(OrdemDeServico ordemDeServico) {
		ordemDeServico.transicionarPara(StatusOrdemDeServico.FINALIZADA, new OrdemDeServicoFinalizadaState());
	}
}
