package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import java.time.LocalDateTime;

class OrdemDeServicoDiagnosticoConcluidoState implements OrdemDeServicoState {

	@Override
	public void enviarOrcamento(OrdemDeServico ordemDeServico) {
		ordemDeServico.setDataEnvioOrcamento(LocalDateTime.now());
		ordemDeServico.transicionarPara(StatusOrdemDeServico.AGUARDANDO_APROVACAO, new OrdemDeServicoAguardandoAprovacaoState());
	}
}
