package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem;

import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemFactory;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;

public class MensagemOrdemEntregueFactory extends MensagemFactory {
    @Override
    public Mensagem criar(MensagemParams params) {
        return new Mensagem(params.getClienteId(),
                "Seu veículo foi entregue. Obrigado pela preferência!");
    }
}
