package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import java.math.BigDecimal;

public record InsumoVinculado(Long insumoId, Integer quantidade, BigDecimal preco) {
	public BigDecimal calculaValorTotal() {
		return preco.multiply(BigDecimal.valueOf(quantidade));
	}
}
