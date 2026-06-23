package com.fiap.mecanica.shared.notificacao.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class MensagemParams {
    private Long clienteId;
    private Long ordemId;
    private BigDecimal valorTotal;
    private String token;
    private String urlAprovar;
    private String urlRecusar;
}
