package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import java.time.LocalDateTime;

class OrdemDeServicoEmExecucaoState implements OrdemDeServicoState {

	@Override
	public void finalizar(OrdemDeServico ordemDeServico) {
		ordemDeServico.setDataFinalizacao(LocalDateTime.now());
		ordemDeServico.transicionarPara(StatusOrdemDeServico.FINALIZADA, new OrdemDeServicoFinalizadaState());
	}
}
