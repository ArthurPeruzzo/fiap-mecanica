package com.fiap.mecanica.shared.notificacao.core.gateway;

import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;

public interface NotificacaoGateway {
    void enviar(Mensagem mensagem);
}
