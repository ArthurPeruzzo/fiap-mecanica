package com.fiap.mecanica.ordemdeservico.core.domain.mensagem;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrcamentoEnviadoFactory;
import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MensagemOrcamentoEnviadoFactoryUnitTest {

    private final MensagemOrcamentoEnviadoFactory factory = new MensagemOrcamentoEnviadoFactory();

    private static final Long CLIENTE_ID = 1L;
    private static final BigDecimal VALOR_TOTAL = new BigDecimal("350.00");
    private static final String TOKEN = "abc-token-123";
    private static final String URL_APROVAR = "http://localhost:8080/ordem-servico/orcamento/externo/aprovar/";
    private static final String URL_RECUSAR = "http://localhost:8080/ordem-servico/orcamento/externo/recusar/";

    @Test
    void shouldCriarMensagemCorreta() {
        var params = MensagemParams.builder()
                .clienteId(CLIENTE_ID)
                .ordemId(10L)
                .valorTotal(VALOR_TOTAL)
                .token(TOKEN)
                .urlAprovar(URL_APROVAR)
                .urlRecusar(URL_RECUSAR)
                .build();

        Mensagem mensagem = factory.criar(params);

        String conteudoEsperado = "Orçamento de R$ " + VALOR_TOTAL + " enviado." +
                " Para aprovar acesse: " + URL_APROVAR + "/" + TOKEN +
                " | Para recusar acesse: " + URL_RECUSAR + "/" + TOKEN;

        assertAll(
                () -> assertEquals(CLIENTE_ID, mensagem.getClienteId()),
                () -> assertEquals(conteudoEsperado, mensagem.getConteudo())
        );
    }
}
