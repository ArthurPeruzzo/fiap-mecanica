package com.fiap.mecanica.ordemdeservico.core.domain.mensagem;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrdemDeServicoRecebidaFactory;
import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MensagemOrdemDeServicoRecebidaFactoryUnitTest {

    private final MensagemOrdemDeServicoRecebidaFactory factory = new MensagemOrdemDeServicoRecebidaFactory();

    @Test
    void shouldCriarMensagemCorreta() {
        var params = MensagemParams.builder()
                .clienteId(1L)
                .ordemId(10L)
                .build();

        Mensagem mensagem = factory.criar(params);

        assertAll(
                () -> assertEquals(1L, mensagem.getClienteId()),
                () -> assertEquals("Sua ordem de serviço foi recebida! Em breve você receberá novas atualizações.", mensagem.getConteudo())
        );
    }
}
