package com.fiap.mecanica.ordemdeservico.core.domain;

import java.time.LocalDateTime;

class OrdemDeServicoEmDiagnosticoState implements OrdemDeServicoState {

	@Override
	public void concluirDiagnostico(OrdemDeServico ordemDeServico) {
		ordemDeServico.setDataConclusaoDiagnostico(LocalDateTime.now());
		ordemDeServico.transicionarPara(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, new OrdemDeServicoDiagnosticoConcluidoState());
	}
}
