package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class PagamentoComandosOutboxGatewayUnitTest {

    @Mock
    private OutboxRegistrador registrador;

    @Test
    void shouldRegistrarPedidoDePagamentoNoExchangeDaCobranca() {
        var gateway = new PagamentoComandosOutboxGateway(registrador, "cobranca.comandos", "pagamento.solicitar");

        gateway.solicitarPagamento(42L, 7L, new BigDecimal("345.50"));

        Mockito.verify(registrador).registrar("SOLICITAR_PAGAMENTO", "cobranca.comandos", "pagamento.solicitar", 42L,
                "{\"ordemServicoId\":42,\"clienteId\":7,\"valor\":345.50}");
    }

    @Test
    void shouldEnviarValorSemNotacaoCientifica() {
        var gateway = new PagamentoComandosOutboxGateway(registrador, "cobranca.comandos", "pagamento.solicitar");

        gateway.solicitarPagamento(1L, 2L, new BigDecimal("1E+3"));

        Mockito.verify(registrador).registrar("SOLICITAR_PAGAMENTO", "cobranca.comandos", "pagamento.solicitar", 1L,
                "{\"ordemServicoId\":1,\"clienteId\":2,\"valor\":1000}");
    }
}
