package com.fiap.mecanica.ordemdeservico.core.gateway;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;

import java.util.Optional;

public interface OrdemDeServicoGateway {
	void criar(OrdemDeServico ordemDeServico);
	Optional<OrdemDeServico> buscarPorId(Long id);
	void atualizar(OrdemDeServico ordemDeServico);
	boolean existeOrdemAbertaParaVeiculo(Long veiculoId);
	void vincularServico(Long ordemServicoId, Long servicoId);
	void desvincularServico(Long ordemServicoId, Long servicoId);
	void vincularOuSomarPeca(Long ordemServicoId, Long pecaId, Integer quantidade);
	void desvincularOuSubtrairPeca(Long ordemServicoId, Long pecaId, Integer quantidade);
	void vincularOuSomarInsumo(Long ordemServicoId, Long insumoId, Integer quantidade);
	void desvincularOuSubtrairInsumo(Long ordemServicoId, Long insumoId, Integer quantidade);
}
