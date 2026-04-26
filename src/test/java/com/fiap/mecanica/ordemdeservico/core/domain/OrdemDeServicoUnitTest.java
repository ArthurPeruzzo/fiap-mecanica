package com.fiap.mecanica.ordemdeservico.core.domain;

import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrdemDeServicoUnitTest {

    @Test
    void shouldCreateOrdemDeServicoWithStatusRecebidaAndDataCriacao() {
        var os = new OrdemDeServico(1L, 2L, 3L);

        assertEquals(1L, os.getClienteId());
        assertEquals(2L, os.getVeiculoId());
        assertEquals(3L, os.getAtendenteId());
        assertEquals(StatusOrdemDeServico.RECEBIDA, os.getStatus());
        assertNotNull(os.getDataCriacao());
        assertNull(os.getId());
        assertNull(os.getMecanicoId());
        assertNull(os.getDataInicioDiagnostico());
        assertNull(os.getDataConclusaoDiagnostico());
    }

    @Test
    void shouldSetDataCriacaoToNow() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = new OrdemDeServico(1L, 2L, 3L);
        var after = LocalDateTime.now().plusSeconds(1);

        assertTrue(os.getDataCriacao().isAfter(before));
        assertTrue(os.getDataCriacao().isBefore(after));
    }

    @Test
    void reconstituir_shouldRestoreAllFields() {
        var dataCriacao = LocalDateTime.of(2026, 1, 10, 9, 0);
        var dataInicio = LocalDateTime.of(2026, 1, 10, 10, 0);
        var dataConclusao = LocalDateTime.of(2026, 1, 10, 11, 0);

        var os = OrdemDeServico.reconstituir(10L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, dataCriacao, dataInicio, dataConclusao);

        assertEquals(10L, os.getId());
        assertEquals(1L, os.getClienteId());
        assertEquals(2L, os.getVeiculoId());
        assertEquals(3L, os.getAtendenteId());
        assertEquals(5L, os.getMecanicoId());
        assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, os.getStatus());
        assertEquals(dataCriacao, os.getDataCriacao());
        assertEquals(dataInicio, os.getDataInicioDiagnostico());
        assertEquals(dataConclusao, os.getDataConclusaoDiagnostico());
    }

    @Test
    void reconstituir_shouldAllowNullDates() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, LocalDateTime.now(), null, null);

        assertNull(os.getMecanicoId());
        assertNull(os.getDataInicioDiagnostico());
        assertNull(os.getDataConclusaoDiagnostico());
    }

    @Test
    void iniciarDiagnostico_shouldSetMecanicoIdAndStatusAndDataInicio() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = new OrdemDeServico(1L, 2L, 3L);

        os.iniciarDiagnostico(7L);

        var after = LocalDateTime.now().plusSeconds(1);
        assertEquals(7L, os.getMecanicoId());
        assertEquals(StatusOrdemDeServico.EM_DIAGNOSTICO, os.getStatus());
        assertNotNull(os.getDataInicioDiagnostico());
        assertTrue(os.getDataInicioDiagnostico().isAfter(before));
        assertTrue(os.getDataInicioDiagnostico().isBefore(after));
    }

    @Test
    void iniciarDiagnostico_shouldThrowWhenStatusIsNotRecebida() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, LocalDateTime.now(), LocalDateTime.now(), null);

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> os.iniciarDiagnostico(7L));
    }

    @Test
    void concluirDiagnostico_shouldSetStatusAndDataConclusao() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, LocalDateTime.now(), LocalDateTime.now(), null);

        os.concluirDiagnostico();

        var after = LocalDateTime.now().plusSeconds(1);
        assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, os.getStatus());
        assertNotNull(os.getDataConclusaoDiagnostico());
        assertTrue(os.getDataConclusaoDiagnostico().isAfter(before));
        assertTrue(os.getDataConclusaoDiagnostico().isBefore(after));
    }

    @Test
    void concluirDiagnostico_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = new OrdemDeServico(1L, 2L, 3L);

        assertThrows(TransicaoDeStatusInvalidaException.class, os::concluirDiagnostico);
    }
}
