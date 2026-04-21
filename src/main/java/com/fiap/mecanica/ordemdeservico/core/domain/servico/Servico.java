package com.fiap.mecanica.ordemdeservico.core.domain.servico;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Servico {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;

    public Servico(String nome, String descricao, BigDecimal preco) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
    }

    public void atualizar(String nome, String descricao, BigDecimal preco) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
    }

    public static Servico reconstituir(Long id, String nome, String descricao, BigDecimal preco) {
        var servico = new Servico(nome, descricao, preco);
        servico.id = id;
        return servico;
    }
}
