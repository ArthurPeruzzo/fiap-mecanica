package com.fiap.mecanica.shared.notificacao.core.gateway;

import java.math.BigDecimal;

public interface NotificacaoGateway {
    void enviarOrcamento(Long clienteId, BigDecimal valorTotal, String token);
    void notificarServicoFinalizado(Long clienteId);
}
