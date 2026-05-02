package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import java.time.LocalDateTime;

class OrdemDeServicoFinalizadaState implements OrdemDeServicoState {

	@Override
	public void entregar(OrdemDeServico ordemDeServico) {
		ordemDeServico.setDataEntrega(LocalDateTime.now());
		ordemDeServico.transicionarPara(StatusOrdemDeServico.ENTREGUE, new OrdemDeServicoEntregueState());
	}
}
