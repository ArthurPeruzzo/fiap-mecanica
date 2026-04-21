package com.fiap.mecanica.gestao.core.domain;

import lombok.Getter;

@Getter
public class Veiculo {

    private Long id;
    private Long clienteId;
    private Placa placa;
    private String modelo;
    private Integer ano;

    public Veiculo(Long clienteId, String placa, String modelo, Integer ano) {
        this.clienteId = clienteId;
        this.placa = new Placa(placa);
        this.modelo = modelo;
        this.ano = ano;
    }

    public void atualizar(String placa, String modelo, Integer ano) {
        this.placa = new Placa(placa);
        this.modelo = modelo;
        this.ano = ano;
    }

    public static Veiculo reconstituir(Long id, Long clienteId, String placa, String modelo, Integer ano) {
        var veiculo = new Veiculo(clienteId, placa, modelo, ano);
        veiculo.id = id;
        return veiculo;
    }
}
