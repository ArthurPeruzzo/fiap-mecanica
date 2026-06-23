package com.fiap.mecanica.shared.notificacao.core.domain;

public abstract class MensagemFactory {
    public abstract Mensagem criar(MensagemParams params);
}
