package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OutboxEventoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OutboxEventoRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Move as linhas pendentes da outbox para o RabbitMQ. A linha só é marcada como enviada depois que o broker
 * confirma que a mensagem chegou a uma fila (publisher confirm sem retorno de "não roteada"); qualquer falha
 * deixa a linha pendente para a próxima rodada. A entrega é, portanto, "pelo menos uma vez": o consumidor
 * precisa tolerar duplicatas.
 * <p>
 * A publicação é um span filho do trace gravado na linha (coluna traceparent) e a mensagem sai com o contexto desse span
 * no cabeçalho traceparent: o consumidor no estoque continua o trace da requisição que gerou o comando.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {

    private final OutboxEventoRepository outboxEventoRepository;
    private final RabbitTemplate rabbitTemplate;
    private final TransactionTemplate transactionTemplate;
    private final Tracer tracer;
    private final Propagator propagator;
    private final String exchange;
    private final String routingKeyLiberarReserva;
    private final int tamanhoDoLote;
    private final Duration timeoutDaConfirmacao;

    public OutboxRelay(OutboxEventoRepository outboxEventoRepository,
                       RabbitTemplate rabbitTemplate,
                       TransactionTemplate transactionTemplate,
                       Tracer tracer,
                       Propagator propagator,
                       @Value("${estoque.mensageria.exchange}") String exchange,
                       @Value("${estoque.mensageria.routing-key.liberar-reserva}") String routingKeyLiberarReserva,
                       @Value("${outbox.relay.lote:50}") int tamanhoDoLote,
                       @Value("${outbox.relay.timeout-confirmacao:5s}") Duration timeoutDaConfirmacao) {
        this.outboxEventoRepository = outboxEventoRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.transactionTemplate = transactionTemplate;
        this.tracer = tracer;
        this.propagator = propagator;
        this.exchange = exchange;
        this.routingKeyLiberarReserva = routingKeyLiberarReserva;
        this.tamanhoDoLote = tamanhoDoLote;
        this.timeoutDaConfirmacao = timeoutDaConfirmacao;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.intervalo:1s}")
    public void publicarPendentes() {
        try {
            transactionTemplate.executeWithoutResult(status -> publicarLote());
        } catch (Exception e) {
            log.error("Falha inesperada no relay da outbox; as linhas seguem pendentes", e);
        }
    }

    private void publicarLote() {
        List<OutboxEventoEntity> pendentes = outboxEventoRepository.buscarPendentesParaEnvio(tamanhoDoLote);
        for (OutboxEventoEntity evento : pendentes) {
            try {
                publicar(evento);
                evento.marcarComoEnviado();
            } catch (Exception e) {
                evento.registrarFalha(e.getMessage());
                log.warn("Outbox: nao foi possivel publicar o evento {} ({} da OS {}), tentativa {}: {}",
                        evento.getId(), evento.getTipo(), evento.getAgregadoId(), evento.getTentativas(), e.getMessage());
                // O broker provavelmente está indisponível: não adianta tentar as demais linhas agora, e a
                // ordem de criação é preservada para a próxima rodada.
                outboxEventoRepository.save(evento);
                return;
            }
            outboxEventoRepository.save(evento);
        }
    }

    private void publicar(OutboxEventoEntity evento) throws Exception {
        Span span = iniciarSpanDePublicacao(evento);
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            publicarMensagem(evento, span);
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /** Span filho do trace gravado na linha; sem traceparent gravado, abre um trace novo. */
    private Span iniciarSpanDePublicacao(OutboxEventoEntity evento) {
        String nome = "outbox publish " + evento.getTipo();
        Span span = evento.getTraceparent() == null
                ? tracer.nextSpan().name(nome)
                : propagator.extract(Map.of(EstoqueComandosOutboxGateway.TRACEPARENT, evento.getTraceparent()), Map::get)
                        .name(nome).kind(Span.Kind.PRODUCER).start();
        return span.tag("outbox.evento.id", evento.getId())
                .tag("outbox.agregado.id", String.valueOf(evento.getAgregadoId()));
    }

    private void publicarMensagem(OutboxEventoEntity evento, Span span) throws Exception {
        Message mensagem = MessageBuilder
                .withBody(evento.getPayload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(evento.getId())
                .setType(evento.getTipo())
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        propagator.inject(span.context(), mensagem.getMessageProperties(),
                (propriedades, chave, valor) -> propriedades.setHeader(chave, valor));

        CorrelationData correlacao = new CorrelationData(evento.getId());
        rabbitTemplate.send(exchange, routingKeyLiberarReserva, mensagem, correlacao);

        CorrelationData.Confirm confirmacao = correlacao.getFuture().get(timeoutDaConfirmacao.toMillis(), TimeUnit.MILLISECONDS);
        if (!confirmacao.ack()) {
            throw new IllegalStateException("broker recusou a mensagem: " + confirmacao.reason());
        }
        if (correlacao.getReturned() != null) {
            // Confirmada pelo broker, mas sem fila ligada à chave (o estoque ainda não declarou a fila).
            throw new IllegalStateException("mensagem nao roteada: " + correlacao.getReturned().getReplyText());
        }
    }
}
