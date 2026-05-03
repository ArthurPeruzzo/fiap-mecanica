package com.fiap.mecanica.ordemdeservico.core.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoVinculadoDto(Long servicoId, BigDecimal preco, String status,
								  LocalDateTime dataInicioExecucao, LocalDateTime dataFimExecucao) {}
