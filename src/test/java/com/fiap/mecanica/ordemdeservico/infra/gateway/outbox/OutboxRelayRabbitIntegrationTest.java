package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OutboxEventoRepository;
import com.fiap.mecanica.resources.testcontainer.AbstractContainer;
import com.fiap.mecanica.resources.testcontainer.RabbitTestContainer;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Relay da outbox contra um RabbitMQ de verdade: publicação com confirmação, mensagem não roteada que
 * permanece pendente e entrega assim que a fila passa a existir.
 */
@ActiveProfiles("integration-test")
@TestPropertySource(properties = {"outbox.relay.enabled=true", "outbox.relay.intervalo=1h",
        "management.tracing.sampling.probability=1.0"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OutboxRelayRabbitIntegrationTest extends AbstractContainer {

    private static final RabbitTestContainer rabbit = new RabbitTestContainer();

    static {
        rabbit.start();
    }

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.virtual-host", () -> "/");
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    private static final String FILA = "teste.liberar-reserva";

    @Autowired private OutboxRelay relay;
    @Autowired private EstoqueComandosGateway estoqueComandosGateway;
    @Autowired private OutboxEventoRepository outboxEventoRepository;
    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private AmqpAdmin amqpAdmin;
    @Autowired private DirectExchange exchange;
    @Autowired private Tracer tracer;

    @Value("${estoque.mensageria.routing-key.liberar-reserva}")
    private String routingKey;

    @AfterEach
    void removerFila() {
        amqpAdmin.deleteQueue(FILA);
    }

    private void ligarFila() {
        Queue fila = new Queue(FILA, false);
        amqpAdmin.declareQueue(fila);
        amqpAdmin.declareBinding(BindingBuilder.bind(fila).to(exchange).with(routingKey));
    }

    @Test
    void shouldPublicarOComandoNaFilaEMarcarComoEnviado() {
        ligarFila();
        estoqueComandosGateway.solicitarLiberacaoDeReservas(9L);
        var pendente = outboxEventoRepository.findAll().getFirst();

        relay.publicarPendentes();

        Message recebida = rabbitTemplate.receive(FILA, 5000);
        assertNotNull(recebida, "o comando deveria ter chegado à fila");
        assertEquals("{\"ordemServicoId\":9}", new String(recebida.getBody(), StandardCharsets.UTF_8));
        assertEquals(pendente.getId(), recebida.getMessageProperties().getMessageId());
        assertEquals("LIBERAR_RESERVA", recebida.getMessageProperties().getType());
        assertEquals(MessageDeliveryMode.PERSISTENT, recebida.getMessageProperties().getReceivedDeliveryMode());

        var enviado = outboxEventoRepository.findById(pendente.getId()).orElseThrow();
        assertNotNull(enviado.getEnviadoEm());
        assertEquals(0, enviado.getTentativas());
        assertEquals(0, outboxEventoRepository.countByEnviadoEmIsNull());
    }

    @Test
    void shouldManterPendenteSeNaoHaFilaLigadaEEntregarQuandoElaExistir() {
        estoqueComandosGateway.solicitarLiberacaoDeReservas(9L);
        var id = outboxEventoRepository.findAll().getFirst().getId();

        relay.publicarPendentes();

        var semFila = outboxEventoRepository.findById(id).orElseThrow();
        assertNull(semFila.getEnviadoEm(), "sem fila ligada a mensagem não pode ser dada como enviada");
        assertEquals(1, semFila.getTentativas());
        assertTrue(semFila.getUltimoErro().contains("nao roteada"), semFila.getUltimoErro());

        ligarFila();
        relay.publicarPendentes();

        assertNotNull(rabbitTemplate.receive(FILA, 5000));
        var entregue = outboxEventoRepository.findById(id).orElseThrow();
        assertNotNull(entregue.getEnviadoEm());
        assertNull(entregue.getUltimoErro());
    }

    @Test
    void shouldLevarOTraceDaRequisicaoAteAMensagemPublicada() {
        ligarFila();
        // Simula a requisição: um span em andamento quando o comando é gravado (como no recusar do orçamento).
        var requisicao = tracer.nextSpan().name("requisicao-de-teste").start();
        String traceIdDaRequisicao = requisicao.context().traceId();
        try (var ignored = tracer.withSpan(requisicao)) {
            estoqueComandosGateway.solicitarLiberacaoDeReservas(9L);
        } finally {
            requisicao.end();
        }
        var gravado = outboxEventoRepository.findAll().getFirst();
        assertNotNull(gravado.getTraceparent(), "o contexto de trace deveria ter sido guardado na outbox");
        assertTrue(gravado.getTraceparent().contains(traceIdDaRequisicao));

        // O relay roda em OUTRA thread, sem requisição nem span ativo: o trace só sobrevive pela coluna da outbox.
        relay.publicarPendentes();

        Message recebida = rabbitTemplate.receive(FILA, 5000);
        assertNotNull(recebida);
        String traceparent = (String) recebida.getMessageProperties().getHeaders().get("traceparent");
        assertNotNull(traceparent, "a mensagem deveria carregar o traceparent");
        var partes = traceparent.split("-");
        assertEquals(traceIdDaRequisicao, partes[1], "mesmo trace da requisição que gerou o comando");
        assertNotEquals(requisicao.context().spanId(), partes[2],
                "a publicação é um span próprio (filho da requisição), não o span da requisição");
    }

    @Test
    void shouldPublicarComTraceNovoQuandoOEventoNaoTemTraceGravado() {
        ligarFila();
        estoqueComandosGateway.solicitarLiberacaoDeReservas(9L); // sem span ativo: nada a guardar
        assertNull(outboxEventoRepository.findAll().getFirst().getTraceparent());

        relay.publicarPendentes();

        Message recebida = rabbitTemplate.receive(FILA, 5000);
        assertNotNull(recebida);
        assertNotNull(recebida.getMessageProperties().getHeaders().get("traceparent"),
                "mesmo sem trace de origem a mensagem sai com um trace próprio");
    }

    @Test
    void shouldEntregarVariosComandosNaOrdemDeCriacao() {
        ligarFila();
        estoqueComandosGateway.solicitarLiberacaoDeReservas(1L);
        estoqueComandosGateway.solicitarLiberacaoDeReservas(2L);
        estoqueComandosGateway.solicitarLiberacaoDeReservas(3L);

        relay.publicarPendentes();

        for (long esperado = 1; esperado <= 3; esperado++) {
            Message recebida = rabbitTemplate.receive(FILA, 5000);
            assertNotNull(recebida);
            assertEquals("{\"ordemServicoId\":" + esperado + "}", new String(recebida.getBody(), StandardCharsets.UTF_8));
        }
    }
}
