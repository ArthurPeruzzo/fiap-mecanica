package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import java.time.LocalDateTime;

/** Orçamento aprovado; a saga de pagamento está em andamento (cobrança e baixa do estoque). */
class OrdemDeServicoAguardandoPagamentoState implements OrdemDeServicoState {

	@Override
	public void iniciarExecucao(OrdemDeServico ordemDeServico) {
		ordemDeServico.transicionarPara(StatusOrdemDeServico.EM_EXECUCAO, new OrdemDeServicoEmExecucaoState());
	}

	// Sem cancelar() manual aqui: com o pagamento em andamento só a resposta da saga encerra a OS. Cancelar à mão
	// deixaria a cobrança e a baixa do estoque sem saber do cancelamento.
	@Override
	public void cancelarPorPagamentoRecusado(OrdemDeServico ordemDeServico) {
		ordemDeServico.setDataCancelamento(LocalDateTime.now());
		ordemDeServico.transicionarPara(StatusOrdemDeServico.CANCELADA, new OrdemDeServicoCanceladaState());
	}
}
