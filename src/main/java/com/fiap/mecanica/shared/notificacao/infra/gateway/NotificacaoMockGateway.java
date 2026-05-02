package com.fiap.mecanica.shared.notificacao.infra.gateway;

import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class NotificacaoMockGateway implements NotificacaoGateway {
    @Override
    public void enviarOrcamento(Long clienteId, BigDecimal valorTotal) {
        log.info("Orçamento de R$ {} enviado ao cliente id {}", valorTotal, clienteId);
    }

    @Override
    public void notificarServicoFinalizado(Long clienteId) {
        log.info("Ordem de servico finalizada. Enviado ao cliente id {}", clienteId);
    }
}
