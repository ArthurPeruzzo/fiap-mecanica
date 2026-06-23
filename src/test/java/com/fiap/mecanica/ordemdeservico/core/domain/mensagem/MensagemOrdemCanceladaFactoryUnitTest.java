package com.fiap.mecanica.ordemdeservico.core.domain.mensagem;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrdemCanceladaFactory;
import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MensagemOrdemCanceladaFactoryUnitTest {

    private final MensagemOrdemCanceladaFactory factory = new MensagemOrdemCanceladaFactory();

    @Test
    void shouldCriarMensagemCorreta() {
        var params = MensagemParams.builder()
                .clienteId(1L)
                .ordemId(10L)
                .build();

        Mensagem mensagem = factory.criar(params);

        assertAll(
                () -> assertEquals(1L, mensagem.getClienteId()),
                () -> assertEquals("Sua ordem de serviço foi cancelada.", mensagem.getConteudo())
        );
    }
}
