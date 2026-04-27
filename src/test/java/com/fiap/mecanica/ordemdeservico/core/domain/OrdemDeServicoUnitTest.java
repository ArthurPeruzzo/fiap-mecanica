package com.fiap.mecanica.ordemdeservico.core.domain;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoJaVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoServicoNaoAutorizadoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemDeServicoUnitTest {

    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnostico() {
        return OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null, List.of());
    }

    @Test
    void shouldCreateOrdemDeServicoWithStatusRecebidaAndDataCriacao() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        assertEquals(1L, os.getClienteId());
        assertEquals(2L, os.getVeiculoId());
        assertEquals(3L, os.getAtendenteId());
        assertEquals(DESCRICAO, os.getDescricao());
        assertEquals(StatusOrdemDeServico.RECEBIDA, os.getStatus());
        assertNotNull(os.getDataCriacao());
        assertNull(os.getId());
        assertNull(os.getMecanicoId());
        assertNull(os.getDataInicioDiagnostico());
        assertNull(os.getDataConclusaoDiagnostico());
        assertTrue(os.getServicoIds().isEmpty());
    }

    @Test
    void shouldSetDataCriacaoToNow() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);
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
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, DESCRICAO, dataCriacao, dataInicio, dataConclusao,
                List.of(7L, 8L));

        assertEquals(10L, os.getId());
        assertEquals(1L, os.getClienteId());
        assertEquals(2L, os.getVeiculoId());
        assertEquals(3L, os.getAtendenteId());
        assertEquals(5L, os.getMecanicoId());
        assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, os.getStatus());
        assertEquals(DESCRICAO, os.getDescricao());
        assertEquals(dataCriacao, os.getDataCriacao());
        assertEquals(dataInicio, os.getDataInicioDiagnostico());
        assertEquals(dataConclusao, os.getDataConclusaoDiagnostico());
        assertEquals(List.of(7L, 8L), os.getServicoIds());
    }

    @Test
    void reconstituir_shouldAllowNullDates() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null, List.of());

        assertNull(os.getMecanicoId());
        assertNull(os.getDataInicioDiagnostico());
        assertNull(os.getDataConclusaoDiagnostico());
    }

    @Test
    void iniciarDiagnostico_shouldSetMecanicoIdAndStatusAndDataInicio() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

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
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null, List.of());

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> os.iniciarDiagnostico(7L));
    }

    @Test
    void concluirDiagnostico_shouldSetStatusAndDataConclusao() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null, List.of());

        os.concluirDiagnostico();

        var after = LocalDateTime.now().plusSeconds(1);
        assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, os.getStatus());
        assertNotNull(os.getDataConclusaoDiagnostico());
        assertTrue(os.getDataConclusaoDiagnostico().isAfter(before));
        assertTrue(os.getDataConclusaoDiagnostico().isBefore(after));
    }

    @Test
    void concluirDiagnostico_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        assertThrows(TransicaoDeStatusInvalidaException.class, os::concluirDiagnostico);
    }

    // --- vincularServico ---

    @Test
    void vincularServico_shouldAddServicoToListWhenStatusIsEmDiagnostico() {
        var os = ordemEmDiagnostico();

        os.vincularServico(10L);

        assertTrue(os.getServicoIds().contains(10L));
    }

    @Test
    void vincularServico_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        assertThrows(VinculoServicoNaoAutorizadoException.class, () -> os.vincularServico(10L));
    }

    @Test
    void vincularServico_shouldThrowWhenServicoAlreadyLinked() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null, List.of(10L));

        assertThrows(ServicoJaVinculadoException.class, () -> os.vincularServico(10L));
    }

    // --- desvincularServico ---

    @Test
    void desvincularServico_shouldRemoveServicoFromListWhenStatusIsEmDiagnostico() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null, List.of(10L));

        os.desvincularServico(10L);

        assertFalse(os.getServicoIds().contains(10L));
    }

    @Test
    void desvincularServico_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null, List.of(10L));

        assertThrows(VinculoServicoNaoAutorizadoException.class, () -> os.desvincularServico(10L));
    }

    @Test
    void desvincularServico_shouldThrowWhenServicoNotLinked() {
        var os = ordemEmDiagnostico();

        assertThrows(ServicoNaoVinculadoException.class, () -> os.desvincularServico(99L));
    }
}
