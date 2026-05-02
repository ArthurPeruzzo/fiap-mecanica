package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoVinculado(Long servicoId, BigDecimal preco, StatusServico status, LocalDateTime dataInicioExecucao, LocalDateTime dataFimExecucao) {
}
