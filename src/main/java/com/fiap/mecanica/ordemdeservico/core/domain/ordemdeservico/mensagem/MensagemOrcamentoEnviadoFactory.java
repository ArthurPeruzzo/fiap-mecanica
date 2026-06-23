package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem;

import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemFactory;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;

public class MensagemOrcamentoEnviadoFactory extends MensagemFactory {

    @Override
    public Mensagem criar(MensagemParams params) {
        return new Mensagem(params.getClienteId(),
                "Orçamento de R$ " + params.getValorTotal() + " enviado." +
                " Para aprovar acesse: " + params.getUrlAprovar() + params.getToken() +
                " | Para recusar acesse: " + params.getUrlRecusar() + params.getToken());
    }
}
