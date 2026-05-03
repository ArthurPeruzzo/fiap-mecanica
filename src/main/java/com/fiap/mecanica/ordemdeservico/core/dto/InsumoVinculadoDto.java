package com.fiap.mecanica.ordemdeservico.core.dto;

import java.math.BigDecimal;

public record InsumoVinculadoDto(Long insumoId, Integer quantidade, BigDecimal preco, BigDecimal valorTotal) {
}
