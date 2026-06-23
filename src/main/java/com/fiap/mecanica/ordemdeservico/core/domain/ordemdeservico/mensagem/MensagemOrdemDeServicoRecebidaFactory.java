package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem;

import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemFactory;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;

public class MensagemOrdemDeServicoRecebidaFactory extends MensagemFactory {
    @Override
    public Mensagem criar(MensagemParams params) {
        return new Mensagem(params.getClienteId(),
                "Sua ordem de serviço foi recebida! Em breve você receberá novas atualizações.");
    }
}
