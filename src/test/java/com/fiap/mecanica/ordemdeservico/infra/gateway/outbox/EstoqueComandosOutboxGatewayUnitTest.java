package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OutboxEventoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OutboxEventoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EstoqueComandosOutboxGatewayUnitTest {

    @InjectMocks
    private EstoqueComandosOutboxGateway gateway;

    @Mock
    private OutboxEventoRepository outboxEventoRepository;

    @Mock
    private Tracer tracer;

    @Mock
    private Propagator propagator;

    @Test
    void shouldGravarComandoDeLiberacaoPendente() {
        gateway.solicitarLiberacaoDeReservas(42L);

        var captor = ArgumentCaptor.forClass(OutboxEventoEntity.class);
        Mockito.verify(outboxEventoRepository).save(captor.capture());
        OutboxEventoEntity evento = captor.getValue();
        assertDoesNotThrow(() -> UUID.fromString(evento.getId()));
        assertEquals("LIBERAR_RESERVA", evento.getTipo());
        assertEquals(42L, evento.getAgregadoId());
        assertEquals("{\"ordemServicoId\":42}", evento.getPayload());
        assertNotNull(evento.getCriadoEm());
        assertNull(evento.getEnviadoEm());
        assertEquals(0, evento.getTentativas());
        assertNull(evento.getTraceparent(), "sem span ativo não há contexto de trace para guardar");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldGuardarOTraceparentDoSpanEmAndamento() {
        Span span = Mockito.mock(Span.class);
        TraceContext contexto = Mockito.mock(TraceContext.class);
        Mockito.when(tracer.currentSpan()).thenReturn(span);
        Mockito.when(span.context()).thenReturn(contexto);
        Mockito.doAnswer(invocacao -> {
            Map<String, String> portador = invocacao.getArgument(1);
            portador.put("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");
            return null;
        }).when(propagator).inject(Mockito.eq(contexto), Mockito.any(), Mockito.any(Propagator.Setter.class));

        gateway.solicitarLiberacaoDeReservas(42L);

        var captor = ArgumentCaptor.forClass(OutboxEventoEntity.class);
        Mockito.verify(outboxEventoRepository).save(captor.capture());
        assertEquals("00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01", captor.getValue().getTraceparent());
    }

    @Test
    void shouldTraduzirFalhaDeGravacaoEmErroDeAcessoAoBanco() {
        Mockito.when(outboxEventoRepository.save(Mockito.any())).thenThrow(new RuntimeException("banco fora"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.solicitarLiberacaoDeReservas(42L));
    }
}
