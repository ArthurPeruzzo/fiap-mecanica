package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EstoqueComandosOutboxGatewayUnitTest {

    @Mock
    private OutboxRegistrador registrador;

    private EstoqueComandosOutboxGateway gateway() {
        return new EstoqueComandosOutboxGateway(registrador, "estoque.comandos", "reserva.liberar", "reserva.confirmar-baixa");
    }

    @Test
    void shouldRegistrarComandoDeLiberacaoNoExchangeDoEstoque() {
        gateway().solicitarLiberacaoDeReservas(42L);

        Mockito.verify(registrador).registrar("LIBERAR_RESERVA", "estoque.comandos", "reserva.liberar", 42L,
                "{\"ordemServicoId\":42}");
    }

    @Test
    void shouldRegistrarComandoDeConfirmacaoDeBaixaNoExchangeDoEstoque() {
        gateway().solicitarConfirmacaoDeBaixa(42L);

        Mockito.verify(registrador).registrar("CONFIRMAR_BAIXA", "estoque.comandos", "reserva.confirmar-baixa", 42L,
                "{\"ordemServicoId\":42}");
    }
}
