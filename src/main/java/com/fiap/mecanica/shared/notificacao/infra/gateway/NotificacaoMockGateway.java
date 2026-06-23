package com.fiap.mecanica.shared.notificacao.infra.gateway;

import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificacaoMockGateway implements NotificacaoGateway {
    @Override
    public void enviar(Mensagem mensagem) {
        log.info(mensagem.getConteudo());
    }
}
