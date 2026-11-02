package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OutboxEventoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OutboxEventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayUnitTest {

    private static final String EXCHANGE = "estoque.comandos";
    private static final String ROUTING_KEY = "reserva.liberar";

    @Mock
    private OutboxEventoRepository outboxEventoRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private TransactionTemplate transactionTemplate;

    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        Mockito.lenient().doAnswer(invocation -> {
            java.util.function.Consumer<org.springframework.transaction.TransactionStatus> acao = invocation.getArgument(0);
            acao.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(Mockito.any());
        // Tracer/Propagator NOOP: estes testes cobrem a publicação; o trace é coberto no teste de integração.
        relay = new OutboxRelay(outboxEventoRepository, rabbitTemplate, transactionTemplate,
                Tracer.NOOP, Propagator.NOOP, EXCHANGE, ROUTING_KEY, 50, Duration.ofMillis(200));
    }

    private OutboxEventoEntity pendente(String id, long ordemId) {
        return OutboxEventoEntity.builder()
                .id(id).tipo("LIBERAR_RESERVA").agregadoId(ordemId)
                .payload("{\"ordemServicoId\":" + ordemId + "}")
                .criadoEm(LocalDateTime.now()).build();
    }

    private void brokerConfirma() {
        Mockito.doAnswer(invocation -> {
            CorrelationData correlacao = invocation.getArgument(3);
            correlacao.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).send(Mockito.eq(EXCHANGE), Mockito.eq(ROUTING_KEY),
                Mockito.any(Message.class), Mockito.any(CorrelationData.class));
    }

    @Test
    void shouldPublicarEMarcarComoEnviadoQuandoBrokerConfirma() {
        var evento = pendente("a", 7L);
        Mockito.when(outboxEventoRepository.buscarPendentesParaEnvio(50)).thenReturn(List.of(evento));
        brokerConfirma();

        relay.publicarPendentes();

        assertNotNull(evento.getEnviadoEm());
        assertEquals(0, evento.getTentativas());
        var mensagem = org.mockito.ArgumentCaptor.forClass(Message.class);
        Mockito.verify(rabbitTemplate).send(Mockito.eq(EXCHANGE), Mockito.eq(ROUTING_KEY), mensagem.capture(), Mockito.any(CorrelationData.class));
        assertEquals("{\"ordemServicoId\":7}", new String(mensagem.getValue().getBody()));
        assertEquals("a", mensagem.getValue().getMessageProperties().getMessageId());
        assertEquals("application/json", mensagem.getValue().getMessageProperties().getContentType());
    }

    @Test
    void shouldManterPendenteERegistrarFalhaQuandoBrokerEstaForaDoAr() {
        var evento = pendente("a", 7L);
        Mockito.when(outboxEventoRepository.buscarPendentesParaEnvio(50)).thenReturn(List.of(evento));
        Mockito.doThrow(new AmqpConnectException(new RuntimeException("recusou conexao")))
                .when(rabbitTemplate).send(Mockito.anyString(), Mockito.anyString(), Mockito.any(Message.class), Mockito.any(CorrelationData.class));

        assertDoesNotThrow(() -> relay.publicarPendentes());

        assertNull(evento.getEnviadoEm());
        assertEquals(1, evento.getTentativas());
        assertNotNull(evento.getUltimoErro());
    }

    @Test
    void shouldInterromperOLoteNaPrimeiraFalhaParaPreservarAOrdem() {
        var primeiro = pendente("a", 7L);
        var segundo = pendente("b", 8L);
        Mockito.when(outboxEventoRepository.buscarPendentesParaEnvio(50)).thenReturn(List.of(primeiro, segundo));
        Mockito.doThrow(new AmqpConnectException(new RuntimeException("fora")))
                .when(rabbitTemplate).send(Mockito.anyString(), Mockito.anyString(), Mockito.any(Message.class), Mockito.any(CorrelationData.class));

        relay.publicarPendentes();

        Mockito.verify(rabbitTemplate, Mockito.times(1)).send(Mockito.anyString(), Mockito.anyString(), Mockito.any(Message.class), Mockito.any(CorrelationData.class));
        assertEquals(0, segundo.getTentativas());
    }

    @Test
    void shouldManterPendenteQuandoBrokerNaoConfirmaNoPrazo() {
        var evento = pendente("a", 7L);
        Mockito.when(outboxEventoRepository.buscarPendentesParaEnvio(50)).thenReturn(List.of(evento));
        // send() não completa o future: a confirmação nunca chega

        relay.publicarPendentes();

        assertNull(evento.getEnviadoEm());
        assertEquals(1, evento.getTentativas());
    }

    @Test
    void shouldManterPendenteQuandoBrokerRecusaAMensagem() {
        var evento = pendente("a", 7L);
        Mockito.when(outboxEventoRepository.buscarPendentesParaEnvio(50)).thenReturn(List.of(evento));
        Mockito.doAnswer(invocation -> {
            CorrelationData correlacao = invocation.getArgument(3);
            correlacao.getFuture().complete(new CorrelationData.Confirm(false, "sem espaco"));
            return null;
        }).when(rabbitTemplate).send(Mockito.anyString(), Mockito.anyString(), Mockito.any(Message.class), Mockito.any(CorrelationData.class));

        relay.publicarPendentes();

        assertNull(evento.getEnviadoEm());
        assertTrue(evento.getUltimoErro().contains("sem espaco"));
    }

    @Test
    void shouldNaoFazerNadaSemPendentes() {
        Mockito.when(outboxEventoRepository.buscarPendentesParaEnvio(50)).thenReturn(List.of());

        relay.publicarPendentes();

        Mockito.verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldNaoPropagarErroInesperadoParaOAgendador() {
        Mockito.when(outboxEventoRepository.buscarPendentesParaEnvio(50)).thenThrow(new RuntimeException("banco fora"));

        assertDoesNotThrow(() -> relay.publicarPendentes());
    }
}
