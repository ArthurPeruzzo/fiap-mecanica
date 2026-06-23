package com.fiap.mecanica.shared.notificacao.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Mensagem {
    private Long clienteId;
    private String conteudo;
}
