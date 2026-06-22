package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class LinkAprovacaoOrcamento {

    private static final int DIAS_EXPIRACAO = 3;

    private Long id;
    private final Long ordemDeServicoId;
    private final String token;
    private final LocalDateTime dataExpiracao;
    private LocalDateTime dataUtilizacao;

    public LinkAprovacaoOrcamento(Long ordemDeServicoId) {
        this.ordemDeServicoId = ordemDeServicoId;
        this.token = UUID.randomUUID().toString();
        this.dataExpiracao = LocalDateTime.now().plusDays(DIAS_EXPIRACAO);
    }

    public boolean estaValido() {
        return dataUtilizacao == null && LocalDateTime.now().isBefore(dataExpiracao);
    }

    public void marcarComoUtilizado() {
        this.dataUtilizacao = LocalDateTime.now();
    }
}
