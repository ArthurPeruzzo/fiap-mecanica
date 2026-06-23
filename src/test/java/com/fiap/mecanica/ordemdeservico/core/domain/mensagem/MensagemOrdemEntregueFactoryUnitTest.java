package com.fiap.mecanica.ordemdeservico.core.domain.mensagem;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrdemEntregueFactory;
import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MensagemOrdemEntregueFactoryUnitTest {

    private final MensagemOrdemEntregueFactory factory = new MensagemOrdemEntregueFactory();

    @Test
    void shouldCriarMensagemCorreta() {
        var params = MensagemParams.builder()
                .clienteId(1L)
                .ordemId(10L)
                .build();

        Mensagem mensagem = factory.criar(params);

        assertAll(
                () -> assertEquals(1L, mensagem.getClienteId()),
                () -> assertEquals("Seu veículo foi entregue. Obrigado pela preferência!", mensagem.getConteudo())
        );
    }
}
