package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OutboxEventoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OutboxEventoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Grava um comando na outbox, qualquer que seja o serviço de destino. Não abre transação própria: participa da que o
 * caso de uso abriu, para que a mudança de status da OS e o comando sejam confirmados (ou descartados) juntos.
 * <p>
 * Guarda também o contexto de trace da requisição em andamento (traceparent): o relay publica depois, em outra thread, e
 * é por essa coluna que o trace atravessa a outbox até o consumidor.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxRegistrador {

    static final String TRACEPARENT = "traceparent";

    private final OutboxEventoRepository outboxEventoRepository;
    private final Tracer tracer;
    private final Propagator propagator;

    public void registrar(String tipo, String exchange, String routingKey, Long ordemServicoId, String payload) {
        try {
            outboxEventoRepository.save(OutboxEventoEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .tipo(tipo)
                    .exchange(exchange)
                    .routingKey(routingKey)
                    .agregadoId(ordemServicoId)
                    .payload(payload)
                    .criadoEm(LocalDateTime.now())
                    .traceparent(traceparentAtual())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao registrar o comando {} da ordem de servico {} na outbox", tipo, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    /** traceparent do span em andamento, ou null quando não há (ex.: fora de uma requisição). */
    private String traceparentAtual() {
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        Map<String, String> portador = new HashMap<>();
        propagator.inject(span.context(), portador, (mapa, chave, valor) -> mapa.put(chave, valor));
        return portador.get(TRACEPARENT);
    }
}
