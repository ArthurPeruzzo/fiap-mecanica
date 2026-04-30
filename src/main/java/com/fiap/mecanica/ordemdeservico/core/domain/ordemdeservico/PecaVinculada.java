package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import java.math.BigDecimal;

public record PecaVinculada(Long pecaId, Integer quantidade, BigDecimal preco) {
}
