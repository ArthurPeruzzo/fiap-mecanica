package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import java.time.LocalDateTime;

class OrdemDeServicoEmDiagnosticoState implements OrdemDeServicoState {

	@Override
	public void concluirDiagnostico(OrdemDeServico ordemDeServico) {
		ordemDeServico.setDataConclusaoDiagnostico(LocalDateTime.now());
		ordemDeServico.calcularOrcamento();
		ordemDeServico.transicionarPara(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, new OrdemDeServicoDiagnosticoConcluidoState());
	}
}
