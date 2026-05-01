package com.fiap.mecanica.ordemdeservico.core.gateway;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrdemDeServicoGateway {
	void criar(OrdemDeServico ordemDeServico);
	Optional<OrdemDeServico> buscarPorId(Long id);
	void atualizar(OrdemDeServico ordemDeServico);
	boolean existeOrdemAbertaParaVeiculo(Long veiculoId);
	void vincularServico(Long ordemServicoId, Long servicoId, BigDecimal preco, StatusServico status);
	void desvincularServico(Long ordemServicoId, Long servicoId);
	void vincularOuSomarPeca(Long ordemServicoId, Long pecaId, Integer quantidade, BigDecimal preco);
	void desvincularOuSubtrairPeca(Long ordemServicoId, Long pecaId, Integer quantidade);
	void vincularOuSomarInsumo(Long ordemServicoId, Long insumoId, Integer quantidade,  BigDecimal preco);
	void desvincularOuSubtrairInsumo(Long ordemServicoId, Long insumoId, Integer quantidade);
}
