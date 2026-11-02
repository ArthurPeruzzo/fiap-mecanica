package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
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
 * Grava o comando na outbox. Não abre transação própria: participa da que o caso de uso abriu, para que a
 * mudança de status da OS e o comando sejam confirmados (ou descartados) juntos.
 * <p>
 * Guarda também o contexto de trace da requisição em andamento (traceparent): o relay publica depois, em outra thread, e
 * é por essa coluna que o trace atravessa a outbox até o consumidor no estoque.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EstoqueComandosOutboxGateway implements EstoqueComandosGateway {

    static final String TIPO_LIBERAR_RESERVA = "LIBERAR_RESERVA";
    static final String TRACEPARENT = "traceparent";

    private final OutboxEventoRepository outboxEventoRepository;
    private final Tracer tracer;
    private final Propagator propagator;

    @Override
    public void solicitarLiberacaoDeReservas(Long ordemServicoId) {
        try {
            outboxEventoRepository.save(OutboxEventoEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .tipo(TIPO_LIBERAR_RESERVA)
                    .agregadoId(ordemServicoId)
                    .payload("{\"ordemServicoId\":" + ordemServicoId + "}")
                    .criadoEm(LocalDateTime.now())
                    .traceparent(traceparentAtual())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao registrar comando de liberacao de reservas da ordem de servico {}", ordemServicoId, e);
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
