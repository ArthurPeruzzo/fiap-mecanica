package com.fiap.mecanica.ordemdeservico.core.domain;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.Orcamento;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.InsumoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.PecaVinculada;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.ServicoVinculado;
import com.fiap.mecanica.ordemdeservico.core.exception.DesvincularInsumoNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.DesvincularPecaNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.MecanicoNaoResponsavelPelaOrdemDeServicoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoMecanicoResponsavelException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoSemServicosException;
import com.fiap.mecanica.ordemdeservico.core.exception.PecaNaoVinculadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.QuantidadeDesvincularInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoJaVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoInsumoNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoPecaNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoServicoNaoAutorizadoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemDeServicoUnitTest {

    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnostico() {
        return OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(), null, null);
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
        assertTrue(os.getServicosVinculados().isEmpty());
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
        var servicosVinculados = List.of(new ServicoVinculado(7L, BigDecimal.TEN), new ServicoVinculado(8L, BigDecimal.TEN));

        var os = OrdemDeServico.reconstituir(10L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, DESCRICAO, dataCriacao, dataInicio, dataConclusao,
                servicosVinculados, List.of(), List.of(), null, null);

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
        assertEquals(List.of(7L, 8L), os.getServicosVinculados().stream().map(ServicoVinculado::servicoId).toList());
    }

    @Test
    void reconstituir_shouldAllowNullDates() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null,
                List.of(), List.of(), List.of(), null, null);

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
    void iniciarDiagnostico_shouldThrowOrdemEmDiagnosticoWhenStatusIsEmDiagnostico() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(), null, null);

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> os.iniciarDiagnostico(7L));
    }

    @Test
    void iniciarDiagnostico_shouldThrowWhenOutroMecanicoJaResponsavel() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null,
                List.of(), List.of(), List.of(), null, null);

        assertThrows(OrdemDeServicoMecanicoResponsavelException.class, () -> os.iniciarDiagnostico(99L));
    }

    @Test
    void iniciarDiagnostico_shouldThrowTransicaoInvalidaWhenStatusIsNotRecebidaOrEmDiagnostico() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(), List.of(), List.of(), null, null);

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> os.iniciarDiagnostico(7L));
    }

    @Test
    void concluirDiagnostico_shouldSetStatusAndDataConclusao() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(new ServicoVinculado(10L, BigDecimal.TEN)), List.of(), List.of(), null, null);

        os.concluirDiagnostico(7L);

        var after = LocalDateTime.now().plusSeconds(1);
        assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, os.getStatus());
        assertNotNull(os.getDataConclusaoDiagnostico());
        assertTrue(os.getDataConclusaoDiagnostico().isAfter(before));
        assertTrue(os.getDataConclusaoDiagnostico().isBefore(after));
        assertNotNull(os.getOrcamento());
    }

    @Test
    void calcularOrcamento_shouldSomarServicoPecaEInsumo() {
        // servico: 50 | peca: 30 × 2 = 60 | insumo: 10 × 3 = 30 | total: 140
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(new ServicoVinculado(10L, new BigDecimal("50"))),
                List.of(new PecaVinculada(20L, 2, new BigDecimal("30"))),
                List.of(new InsumoVinculado(30L, 3, new BigDecimal("10"))),
                null, null);

        os.concluirDiagnostico(7L);

        assertEquals(0, new BigDecimal("140").compareTo(os.getOrcamento().valorTotal()));
    }

    @Test
    void calcularOrcamento_shouldSomarMultiplosPecasEInsumos() {
        // servico: 100 | peca20: 20 × 1 = 20, peca21: 15 × 4 = 60 | insumo30: 5 × 2 = 10 | total: 190
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(new ServicoVinculado(10L, new BigDecimal("100"))),
                List.of(new PecaVinculada(20L, 1, new BigDecimal("20")),
                        new PecaVinculada(21L, 4, new BigDecimal("15"))),
                List.of(new InsumoVinculado(30L, 2, new BigDecimal("5"))),
                null, null);

        os.concluirDiagnostico(7L);

        assertEquals(0, new BigDecimal("190").compareTo(os.getOrcamento().valorTotal()));
    }

    @Test
    void calcularOrcamento_shouldSerApenasServicosQuandoSemPecasEInsumos() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(new ServicoVinculado(10L, new BigDecimal("75"))),
                List.of(), List.of(), null, null);

        os.concluirDiagnostico(7L);

        assertEquals(0, new BigDecimal("75").compareTo(os.getOrcamento().valorTotal()));
    }

    @Test
    void concluirDiagnostico_shouldThrowWhenMecanicoNaoEhResponsavel() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(new ServicoVinculado(10L, BigDecimal.TEN)), List.of(), List.of(), null, null);

        assertThrows(MecanicoNaoResponsavelPelaOrdemDeServicoException.class, () -> os.concluirDiagnostico(99L));
    }

    @Test
    void concluirDiagnostico_shouldThrowWhenSemServicosVinculados() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(), null, null);

        assertThrows(OrdemDeServicoSemServicosException.class, () -> os.concluirDiagnostico(7L));
    }

    @Test
    void concluirDiagnostico_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ServicoVinculado(10L, BigDecimal.TEN)), List.of(), List.of(), null, null);

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> os.concluirDiagnostico(7L));
    }

    // --- vincularServico ---

    @Test
    void vincularServico_shouldAddServicoToListWhenStatusIsEmDiagnostico() {
        var os = ordemEmDiagnostico();

        os.vincularServico(10L, BigDecimal.TEN);

        assertTrue(os.getServicosVinculados().stream().anyMatch(s -> s.servicoId().equals(10L)));
    }

    @Test
    void vincularServico_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        assertThrows(VinculoServicoNaoAutorizadoException.class, () -> os.vincularServico(10L, BigDecimal.TEN));
    }

    @Test
    void vincularServico_shouldThrowWhenServicoAlreadyLinked() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(new ServicoVinculado(10L, BigDecimal.TEN)), List.of(), List.of(), null, null);

        assertThrows(ServicoJaVinculadoException.class, () -> os.vincularServico(10L, BigDecimal.TEN));
    }

    // --- desvincularServico ---

    @Test
    void desvincularServico_shouldRemoveServicoFromListWhenStatusIsEmDiagnostico() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(new ServicoVinculado(10L, BigDecimal.TEN)), List.of(), List.of(), null, null);

        os.desvincularServico(10L);

        assertFalse(os.getServicosVinculados().stream().anyMatch(s -> s.servicoId().equals(10L)));
    }

    @Test
    void desvincularServico_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null,
                List.of(new ServicoVinculado(10L, BigDecimal.TEN)), List.of(), List.of(), null, null);

        assertThrows(VinculoServicoNaoAutorizadoException.class, () -> os.desvincularServico(10L));
    }

    @Test
    void desvincularServico_shouldThrowWhenServicoNotLinked() {
        var os = ordemEmDiagnostico();

        assertThrows(ServicoNaoVinculadoException.class, () -> os.desvincularServico(99L));
    }

    // --- vincularPeca ---

    @Test
    void vincularPeca_shouldAddPecaToListWhenStatusIsEmDiagnostico() {
        var os = ordemEmDiagnostico();

        BigDecimal preco = new BigDecimal(10);
        os.vincularPeca(20L, 3, preco);

        assertEquals(1, os.getPecasVinculadas().size());
        assertEquals(20L, os.getPecasVinculadas().getFirst().pecaId());
        assertEquals(3, os.getPecasVinculadas().getFirst().quantidade());
        assertEquals(preco, os.getPecasVinculadas().getFirst().preco());
    }

    @Test
    void vincularPeca_shouldSomarQuantidadeWhenPecaAlreadyLinked() {
        BigDecimal preco = new BigDecimal(10);
        var pecaExistente = new PecaVinculada(20L, 2, preco);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(pecaExistente), List.of(), null, null);

        os.vincularPeca(20L, 3, preco);

        assertEquals(1, os.getPecasVinculadas().size());
        assertEquals(20L, os.getPecasVinculadas().getFirst().pecaId());
        assertEquals(5, os.getPecasVinculadas().getFirst().quantidade());
        assertEquals(preco, os.getPecasVinculadas().getFirst().preco());
    }

    @Test
    void vincularPeca_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);
        BigDecimal preco = new BigDecimal(10);
        assertThrows(VinculoPecaNaoAutorizadaException.class, () -> os.vincularPeca(20L, 3, preco));
    }

    // --- desvincularPeca ---

    @Test
    void desvincularPeca_shouldRemovePecaWhenQuantidadeIgualAVinculada() {
        BigDecimal preco = new BigDecimal(10);
        var pecaExistente = new PecaVinculada(20L, 3, preco);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(pecaExistente), List.of(), null, null);

        os.desvincularPeca(20L, 3);

        assertTrue(os.getPecasVinculadas().isEmpty());
    }

    @Test
    void desvincularPeca_shouldSubtrairQuantidadeWhenParcial() {
        BigDecimal preco = new BigDecimal(10);
        var pecaExistente = new PecaVinculada(20L, 5, preco);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(pecaExistente), List.of(), null, null);

        os.desvincularPeca(20L, 2);

        assertEquals(1, os.getPecasVinculadas().size());
        assertEquals(3, os.getPecasVinculadas().getFirst().quantidade());
    }

    @Test
    void desvincularPeca_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        assertThrows(DesvincularPecaNaoAutorizadaException.class, () -> os.desvincularPeca(20L, 1));
    }

    @Test
    void desvincularPeca_shouldThrowWhenPecaNaoVinculada() {
        var os = ordemEmDiagnostico();

        assertThrows(PecaNaoVinculadaException.class, () -> os.desvincularPeca(99L, 1));
    }

    @Test
    void desvincularPeca_shouldThrowWhenQuantidadeMaiorQueVinculada() {
        BigDecimal preco = new BigDecimal(10);
        var pecaExistente = new PecaVinculada(20L, 2, preco);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(pecaExistente), List.of(), null, null);

        assertThrows(QuantidadeDesvincularInvalidaException.class, () -> os.desvincularPeca(20L, 5));
    }

    // --- vincularInsumo ---

    @Test
    void vincularInsumo_shouldAddInsumoToListWhenStatusIsEmDiagnostico() {
        var os = ordemEmDiagnostico();

        BigDecimal preco = new BigDecimal(10);
        os.vincularInsumo(30L, 5, preco);

        assertEquals(1, os.getInsumosVinculados().size());
        assertEquals(30L, os.getInsumosVinculados().getFirst().insumoId());
        assertEquals(5, os.getInsumosVinculados().getFirst().quantidade());
        assertEquals(preco, os.getInsumosVinculados().getFirst().preco());
    }

    @Test
    void vincularInsumo_shouldSomarQuantidadeWhenInsumoAlreadyLinked() {
        BigDecimal preco = new BigDecimal(10);
        var insumoExistente = new InsumoVinculado(30L, 4, preco);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(insumoExistente), null, null);

        os.vincularInsumo(30L, 3, preco);

        assertEquals(1, os.getInsumosVinculados().size());
        assertEquals(30L, os.getInsumosVinculados().getFirst().insumoId());
        assertEquals(7, os.getInsumosVinculados().getFirst().quantidade());
        assertEquals(preco, os.getInsumosVinculados().getFirst().preco());
    }

    @Test
    void vincularInsumo_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        assertThrows(VinculoInsumoNaoAutorizadaException.class, () -> os.vincularInsumo(30L, 5, BigDecimal.TEN));
    }

    // --- desvincularInsumo ---

    @Test
    void desvincularInsumo_shouldRemoveInsumoWhenQuantidadeIgualAVinculada() {
        var insumoExistente = new InsumoVinculado(30L, 4, BigDecimal.TEN);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(insumoExistente), null, null);

        os.desvincularInsumo(30L, 4);

        assertTrue(os.getInsumosVinculados().isEmpty());
    }

    @Test
    void desvincularInsumo_shouldSubtrairQuantidadeWhenParcial() {
        var insumoExistente = new InsumoVinculado(30L, 6, BigDecimal.TEN);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(insumoExistente), null, null);

        os.desvincularInsumo(30L, 2);

        assertEquals(1, os.getInsumosVinculados().size());
        assertEquals(4, os.getInsumosVinculados().getFirst().quantidade());
    }

    @Test
    void desvincularInsumo_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        assertThrows(DesvincularInsumoNaoAutorizadaException.class, () -> os.desvincularInsumo(30L, 1));
    }

    @Test
    void desvincularInsumo_shouldThrowWhenInsumoNaoVinculado() {
        var os = ordemEmDiagnostico();

        assertThrows(InsumoNaoVinculadoException.class, () -> os.desvincularInsumo(99L, 1));
    }

    @Test
    void desvincularInsumo_shouldThrowWhenQuantidadeMaiorQueVinculada() {
        var insumoExistente = new InsumoVinculado(30L, 2, BigDecimal.TEN);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(insumoExistente), null, null);

        assertThrows(QuantidadeDesvincularInvalidaException.class, () -> os.desvincularInsumo(30L, 5));
    }

    // --- gravarEnvioOrcamento ---

    @Test
    void gravarEnvioOrcamento_shouldSetDataEnvioETransicionarParaAguardandoAprovacao() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ServicoVinculado(10L, BigDecimal.TEN)), List.of(), List.of(),
                new Orcamento(new BigDecimal("100")), null);

        os.gravarEnvioOrcamento();

        var after = LocalDateTime.now().plusSeconds(1);
        assertEquals(StatusOrdemDeServico.AGUARDANDO_APROVACAO, os.getStatus());
        assertNotNull(os.getDataEnvioOrcamento());
        assertTrue(os.getDataEnvioOrcamento().isAfter(before));
        assertTrue(os.getDataEnvioOrcamento().isBefore(after));
    }

    @Test
    void gravarEnvioOrcamento_shouldThrowWhenStatusIsNotDiagnosticoConcluido() {
        var os = OrdemDeServico.reconstituir(1L, 1L, 2L, 3L, 7L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(), null, null);

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> os.gravarEnvioOrcamento());
    }
}
