package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import java.math.BigDecimal;

public record ServicoResponseJson(Long id, String nome, String descricao, BigDecimal preco) {}
