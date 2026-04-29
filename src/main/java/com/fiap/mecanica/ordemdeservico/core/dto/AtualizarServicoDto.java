package com.fiap.mecanica.ordemdeservico.core.dto;

import java.math.BigDecimal;

public record AtualizarServicoDto(Long id, String nome, String descricao, BigDecimal preco) {
}
