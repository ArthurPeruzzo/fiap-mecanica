package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OutboxEventoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OutboxEventoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EstoqueComandosOutboxGatewayUnitTest {

    @InjectMocks
    private EstoqueComandosOutboxGateway gateway;

    @Mock
    private OutboxEventoRepository outboxEventoRepository;

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
    }

    @Test
    void shouldTraduzirFalhaDeGravacaoEmErroDeAcessoAoBanco() {
        Mockito.when(outboxEventoRepository.save(Mockito.any())).thenThrow(new RuntimeException("banco fora"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.solicitarLiberacaoDeReservas(42L));
    }
}
