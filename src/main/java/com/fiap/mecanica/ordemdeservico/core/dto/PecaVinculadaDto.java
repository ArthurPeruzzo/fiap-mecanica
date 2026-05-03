package com.fiap.mecanica.ordemdeservico.core.dto;

import java.math.BigDecimal;

public record PecaVinculadaDto(Long pecaId, Integer quantidade, BigDecimal preco, BigDecimal valorTotal) {
}
