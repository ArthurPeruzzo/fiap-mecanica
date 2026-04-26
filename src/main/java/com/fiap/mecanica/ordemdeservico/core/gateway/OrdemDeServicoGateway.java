package com.fiap.mecanica.ordemdeservico.core.gateway;

import com.fiap.mecanica.ordemdeservico.core.domain.OrdemDeServico;

import java.util.Optional;

public interface OrdemDeServicoGateway {
	void criar(OrdemDeServico ordemDeServico);
	Optional<OrdemDeServico> buscarPorId(Long id);
	void atualizar(OrdemDeServico ordemDeServico);
	boolean existeOrdemAbertaParaVeiculo(Long veiculoId);
}
