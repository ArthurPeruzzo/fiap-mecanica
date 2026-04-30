package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import java.math.BigDecimal;

public record ServicoVinculado(Long servicoId, BigDecimal preco) {
}
