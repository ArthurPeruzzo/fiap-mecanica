package com.fiap.mecanica.gestao.core.domain;

import lombok.Getter;

@Getter
public class Placa {

    private final String valor;

    public Placa(String valor) {
        this.valor = valor.replace("-", "").toUpperCase();
    }

    public String getValorFormatado() {
        return valor.replaceAll("^([A-Z]{3})(.+)$", "$1-$2");
    }
}
