package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;

import java.math.BigDecimal;

public record ServicoResponseJson(Long id, String nome, String descricao, BigDecimal preco) {

    public static ServicoResponseJson from(Servico servico) {
        return new ServicoResponseJson(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPreco()
        );
    }
}
