package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem;

import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemFactory;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;

public class MensagemOrdemFinalizadaFactory extends MensagemFactory {
    @Override
    public Mensagem criar(MensagemParams params) {
        return new Mensagem(params.getClienteId(),
                "Todos os serviços foram concluídos. Sua ordem de serviço está pronta para entrega.");
    }
}
