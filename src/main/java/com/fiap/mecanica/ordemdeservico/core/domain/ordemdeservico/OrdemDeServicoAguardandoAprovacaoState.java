package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import java.time.LocalDateTime;

class OrdemDeServicoAguardandoAprovacaoState implements OrdemDeServicoState {

	@Override
	public void cancelar(OrdemDeServico ordemDeServico) {
		ordemDeServico.setDataCancelamento(LocalDateTime.now());
		ordemDeServico.transicionarPara(StatusOrdemDeServico.CANCELADA, new OrdemDeServicoCanceladaState());
	}
}
