package com.fiap.mecanica.ordemdeservico.core.domain;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemDeServicoUnitTest {

    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnostico() {
        return OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();
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
        var servicosVinculados = List.of(new ServicoVinculado(7L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null),
                new ServicoVinculado(8L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null));

        var os = OrdemDeServico.builder()
                .id(10L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(dataCriacao)
                .dataInicioDiagnostico(dataInicio)
                .dataConclusaoDiagnostico(dataConclusao)
                .servicosVinculados(new ArrayList<>(servicosVinculados))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();

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
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(null)
                .status(StatusOrdemDeServico.RECEBIDA)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(null)
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.RECEBIDA))
                .build();

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
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> os.iniciarDiagnostico(7L));
    }

    @Test
    void iniciarDiagnostico_shouldThrowWhenOutroMecanicoJaResponsavel() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.RECEBIDA)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(null)
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.RECEBIDA))
                .build();

        assertThrows(OrdemDeServicoMecanicoResponsavelException.class, () -> os.iniciarDiagnostico(99L));
    }

    @Test
    void iniciarDiagnostico_shouldThrowTransicaoInvalidaWhenStatusIsNotRecebidaOrEmDiagnostico() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();

        assertThrows(TransicaoDeStatusInvalidaException.class, () -> os.iniciarDiagnostico(7L));
    }

    @Test
    void concluirDiagnostico_shouldSetStatusAndDataConclusao() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

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
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, new BigDecimal("50"), StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of(new PecaVinculada(20L, 2, new BigDecimal("30")))))
                .insumosVinculados(new ArrayList<>(List.of(new InsumoVinculado(30L, 3, new BigDecimal("10")))))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        os.concluirDiagnostico(7L);

        assertEquals(0, new BigDecimal("140").compareTo(os.getOrcamento().valorTotal()));
    }

    @Test
    void calcularOrcamento_shouldSomarMultiplosPecasEInsumos() {
        // servico: 100 | peca20: 20 × 1 = 20, peca21: 15 × 4 = 60 | insumo30: 5 × 2 = 10 | total: 190
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, new BigDecimal("100"), StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of(new PecaVinculada(20L, 1, new BigDecimal("20")),
                        new PecaVinculada(21L, 4, new BigDecimal("15")))))
                .insumosVinculados(new ArrayList<>(List.of(new InsumoVinculado(30L, 2, new BigDecimal("5")))))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        os.concluirDiagnostico(7L);

        assertEquals(0, new BigDecimal("190").compareTo(os.getOrcamento().valorTotal()));
    }

    @Test
    void calcularOrcamento_shouldSerApenasServicosQuandoSemPecasEInsumos() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, new BigDecimal("75"), StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        os.concluirDiagnostico(7L);

        assertEquals(0, new BigDecimal("75").compareTo(os.getOrcamento().valorTotal()));
    }

    @Test
    void concluirDiagnostico_shouldThrowWhenMecanicoNaoEhResponsavel() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        assertThrows(MecanicoNaoResponsavelPelaOrdemDeServicoException.class, () -> os.concluirDiagnostico(99L));
    }

    @Test
    void concluirDiagnostico_shouldThrowWhenSemServicosVinculados() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        assertThrows(OrdemDeServicoSemServicosException.class, () -> os.concluirDiagnostico(7L));
    }

    @Test
    void concluirDiagnostico_shouldThrowWhenStatusIsNotEmDiagnostico() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();

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
    void vincularServico_shouldAddServicoToListWhenStatusIsRecebida() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        os.vincularServico(10L, BigDecimal.TEN);

        assertTrue(os.getServicosVinculados().stream().anyMatch(s -> s.servicoId().equals(10L)));
    }

    @Test
    void vincularServico_shouldThrowWhenStatusIsNotEmDiagnosticoNemRecebida() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();

        assertThrows(VinculoServicoNaoAutorizadoException.class, () -> os.vincularServico(10L, BigDecimal.TEN));
    }

    @Test
    void vincularServico_shouldThrowWhenServicoAlreadyLinked() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        assertThrows(ServicoJaVinculadoException.class, () -> os.vincularServico(10L, BigDecimal.TEN));
    }

    // --- desvincularServico ---

    @Test
    void desvincularServico_shouldRemoveServicoFromListWhenStatusIsEmDiagnostico() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        os.desvincularServico(10L);

        assertFalse(os.getServicosVinculados().stream().anyMatch(s -> s.servicoId().equals(10L)));
    }

    @Test
    void desvincularServico_shouldThrowWhenStatusIsNotEmDiagnosticoNemRecebida() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(null)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();

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
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of(pecaExistente)))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        os.vincularPeca(20L, 3, preco);

        assertEquals(1, os.getPecasVinculadas().size());
        assertEquals(20L, os.getPecasVinculadas().getFirst().pecaId());
        assertEquals(5, os.getPecasVinculadas().getFirst().quantidade());
        assertEquals(preco, os.getPecasVinculadas().getFirst().preco());
    }

    @Test
    void vincularPeca_shouldAddPecaToListWhenStatusIsRecebida() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        os.vincularPeca(20L, 3, new BigDecimal(10));

        assertEquals(1, os.getPecasVinculadas().size());
        assertEquals(20L, os.getPecasVinculadas().getFirst().pecaId());
    }

    @Test
    void vincularPeca_shouldThrowWhenStatusIsNotEmDiagnosticoNemRecebida() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();
        assertThrows(VinculoPecaNaoAutorizadaException.class, () -> os.vincularPeca(20L, 3, BigDecimal.TEN));
    }

    // --- desvincularPeca ---

    @Test
    void desvincularPeca_shouldRemovePecaWhenQuantidadeIgualAVinculada() {
        BigDecimal preco = new BigDecimal(10);
        var pecaExistente = new PecaVinculada(20L, 3, preco);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of(pecaExistente)))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        os.desvincularPeca(20L, 3);

        assertTrue(os.getPecasVinculadas().isEmpty());
    }

    @Test
    void desvincularPeca_shouldSubtrairQuantidadeWhenParcial() {
        BigDecimal preco = new BigDecimal(10);
        var pecaExistente = new PecaVinculada(20L, 5, preco);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of(pecaExistente)))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

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
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of(pecaExistente)))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

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
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of(insumoExistente)))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        os.vincularInsumo(30L, 3, preco);

        assertEquals(1, os.getInsumosVinculados().size());
        assertEquals(30L, os.getInsumosVinculados().getFirst().insumoId());
        assertEquals(7, os.getInsumosVinculados().getFirst().quantidade());
        assertEquals(preco, os.getInsumosVinculados().getFirst().preco());
    }

    @Test
    void vincularInsumo_shouldAddInsumoToListWhenStatusIsRecebida() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);

        os.vincularInsumo(30L, 5, BigDecimal.TEN);

        assertEquals(1, os.getInsumosVinculados().size());
        assertEquals(30L, os.getInsumosVinculados().getFirst().insumoId());
    }

    @Test
    void vincularInsumo_shouldThrowWhenStatusIsNotEmDiagnosticoNemRecebida() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();
        assertThrows(VinculoInsumoNaoAutorizadaException.class, () -> os.vincularInsumo(30L, 5, BigDecimal.TEN));
    }

    // --- desvincularInsumo ---

    @Test
    void desvincularInsumo_shouldRemoveInsumoWhenQuantidadeIgualAVinculada() {
        var insumoExistente = new InsumoVinculado(30L, 4, BigDecimal.TEN);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of(insumoExistente)))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        os.desvincularInsumo(30L, 4);

        assertTrue(os.getInsumosVinculados().isEmpty());
    }

    @Test
    void desvincularInsumo_shouldSubtrairQuantidadeWhenParcial() {
        var insumoExistente = new InsumoVinculado(30L, 6, BigDecimal.TEN);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of(insumoExistente)))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

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
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of(insumoExistente)))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        assertThrows(QuantidadeDesvincularInvalidaException.class, () -> os.desvincularInsumo(30L, 5));
    }

    // --- iniciarServico ---

    private OrdemDeServico ordemEmExecucaoComServico(Long servicoId, StatusServico statusServico) {
        return OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(servicoId, BigDecimal.TEN, statusServico, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(BigDecimal.TEN))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(LocalDateTime.now())
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();
    }

    @Test
    void iniciarServico_shouldSetStatusEmExecucaoEDataInicioExecucao() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = ordemEmExecucaoComServico(10L, StatusServico.NAO_INICIADO);

        os.iniciarServico(10L);

        var after = LocalDateTime.now().plusSeconds(1);
        var servicoAtualizado = os.getServicosVinculados().stream()
                .filter(s -> s.servicoId().equals(10L)).findFirst().orElseThrow();
        assertEquals(StatusServico.EM_EXECUCAO, servicoAtualizado.status());
        assertNotNull(servicoAtualizado.dataInicioExecucao());
        assertTrue(servicoAtualizado.dataInicioExecucao().isAfter(before));
        assertTrue(servicoAtualizado.dataInicioExecucao().isBefore(after));
        assertNull(servicoAtualizado.dataFimExecucao());
    }

    @Test
    void iniciarServico_shouldPreservePrecoOnUpdate() {
        var preco = new BigDecimal("150.00");
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, preco, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(preco))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(LocalDateTime.now())
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();

        os.iniciarServico(10L);

        var servicoAtualizado = os.getServicosVinculados().stream()
                .filter(s -> s.servicoId().equals(10L)).findFirst().orElseThrow();
        assertEquals(0, preco.compareTo(servicoAtualizado.preco()));
    }

    @Test
    void iniciarServico_shouldThrowWhenOrdemNaoEmExecucao() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        assertThrows(IniciarServicoNaoAutorizadoException.class, () -> os.iniciarServico(10L));
    }

    @Test
    void iniciarServico_shouldThrowWhenServicoNaoVinculado() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(BigDecimal.TEN))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(LocalDateTime.now())
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();

        assertThrows(ServicoNaoVinculadoException.class, () -> os.iniciarServico(99L));
    }

    @Test
    void iniciarServico_shouldThrowWhenServicoJaIniciado() {
        var os = ordemEmExecucaoComServico(10L, StatusServico.EM_EXECUCAO);

        assertThrows(ServicoEmExecucaoOuFinalizadoException.class, () -> os.iniciarServico(10L));
    }

    @Test
    void iniciarServico_shouldThrowWhenServicoFinalizado() {
        var os = ordemEmExecucaoComServico(10L, StatusServico.FINALIZADO);

        assertThrows(ServicoEmExecucaoOuFinalizadoException.class, () -> os.iniciarServico(10L));
    }

    // --- finalizarServico ---

    @Test
    void finalizarServico_shouldSetStatusFinalizadoEDataFimExecucao() {
        var before = LocalDateTime.now().minusSeconds(1);
        var dataInicio = LocalDateTime.now().minusHours(1);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.EM_EXECUCAO, dataInicio, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(BigDecimal.TEN))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(LocalDateTime.now())
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();

        os.finalizarServico(10L);

        var after = LocalDateTime.now().plusSeconds(1);
        var servicoAtualizado = os.getServicosVinculados().stream()
                .filter(s -> s.servicoId().equals(10L)).findFirst().orElseThrow();
        assertEquals(StatusServico.FINALIZADO, servicoAtualizado.status());
        assertNotNull(servicoAtualizado.dataFimExecucao());
        assertTrue(servicoAtualizado.dataFimExecucao().isAfter(before));
        assertTrue(servicoAtualizado.dataFimExecucao().isBefore(after));
    }

    @Test
    void finalizarServico_shouldPreserveDataInicioExecucaoAndPreco() {
        var dataInicio = LocalDateTime.now().minusHours(2);
        var preco = new BigDecimal("250.00");
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, preco, StatusServico.EM_EXECUCAO, dataInicio, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(preco))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(LocalDateTime.now())
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();

        os.finalizarServico(10L);

        var servicoAtualizado = os.getServicosVinculados().stream()
                .filter(s -> s.servicoId().equals(10L)).findFirst().orElseThrow();
        assertEquals(dataInicio, servicoAtualizado.dataInicioExecucao());
        assertEquals(0, preco.compareTo(servicoAtualizado.preco()));
    }

    @Test
    void finalizarServico_shouldTransicionarOSParaFinalizadaQuandoUltimoServico() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.EM_EXECUCAO, LocalDateTime.now().minusHours(1), null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(BigDecimal.TEN))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(LocalDateTime.now())
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();

        os.finalizarServico(10L);

        assertEquals(StatusOrdemDeServico.FINALIZADA, os.getStatus());
    }

    @Test
    void finalizarServico_shouldNaoTransicionarOSQuandoServicosRestantesNaoFinalizados() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(
                        new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.EM_EXECUCAO, LocalDateTime.now().minusHours(1), null),
                        new ServicoVinculado(20L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null)
                )))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(BigDecimal.TEN))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(LocalDateTime.now())
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();

        os.finalizarServico(10L);

        assertEquals(StatusOrdemDeServico.EM_EXECUCAO, os.getStatus());
    }

    @Test
    void finalizarServico_shouldThrowWhenOrdemNaoEmExecucao() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.EM_EXECUCAO, LocalDateTime.now(), null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        assertThrows(FinalizarServicoNaoAutorizadoException.class, () -> os.finalizarServico(10L));
    }

    @Test
    void finalizarServico_shouldThrowWhenServicoNaoVinculado() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(BigDecimal.TEN))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(LocalDateTime.now())
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();

        assertThrows(ServicoNaoVinculadoException.class, () -> os.finalizarServico(99L));
    }

    @Test
    void finalizarServico_shouldThrowWhenServicoNaoIniciado() {
        var os = ordemEmExecucaoComServico(10L, StatusServico.NAO_INICIADO);

        assertThrows(ServicoNaoIniciadoOuFinalizadoException.class, () -> os.finalizarServico(10L));
    }

    @Test
    void finalizarServico_shouldThrowWhenServicoJaFinalizado() {
        var os = ordemEmExecucaoComServico(10L, StatusServico.FINALIZADO);

        assertThrows(ServicoNaoIniciadoOuFinalizadoException.class, () -> os.finalizarServico(10L));
    }

    // --- gravarEnvioOrcamento ---

    @Test
    void gravarEnvioOrcamento_shouldSetDataEnvioETransicionarParaAguardandoAprovacao() {
        var before = LocalDateTime.now().minusSeconds(1);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(new BigDecimal("100")))
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();

        os.gravarEnvioOrcamento();

        var after = LocalDateTime.now().plusSeconds(1);
        assertEquals(StatusOrdemDeServico.AGUARDANDO_APROVACAO, os.getStatus());
        assertNotNull(os.getDataEnvioOrcamento());
        assertTrue(os.getDataEnvioOrcamento().isAfter(before));
        assertTrue(os.getDataEnvioOrcamento().isBefore(after));
    }

    @Test
    void gravarEnvioOrcamento_shouldThrowWhenStatusIsNotDiagnosticoConcluido() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(7L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();

        assertThrows(TransicaoDeStatusInvalidaException.class, os::gravarEnvioOrcamento);
    }

    // --- calcularTempoMedioExecucaoServicos ---

    @Test
    void calcularTempoMedioExecucaoServicos_shouldReturnNullWhenNoServicosVinculados() {
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.FINALIZADA)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(LocalDateTime.now())
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.FINALIZADA))
                .build();

        assertNull(os.calcularTempoMedioExecucaoServicos());
    }

    @Test
    void calcularTempoMedioExecucaoServicos_shouldReturnNullWhenNoServicosFinalizados() {
        var inicio = LocalDateTime.of(2024, 1, 15, 10, 0);
        var servico = new ServicoVinculado(1L, new BigDecimal("100.00"), StatusServico.EM_EXECUCAO, inicio, null);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(servico)))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();

        assertNull(os.calcularTempoMedioExecucaoServicos());
    }

    @Test
    void calcularTempoMedioExecucaoServicos_shouldReturnCorrectDurationForSingleServico() {
        var inicio = LocalDateTime.of(2024, 1, 15, 10, 0);
        var fim = LocalDateTime.of(2024, 1, 15, 12, 30);
        var servico = new ServicoVinculado(1L, new BigDecimal("100.00"), StatusServico.FINALIZADO, inicio, fim);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.FINALIZADA)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(servico)))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(LocalDateTime.now())
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.FINALIZADA))
                .build();

        var resultado = os.calcularTempoMedioExecucaoServicos();

        assertNotNull(resultado);
        assertEquals(0, resultado.toDaysPart());
        assertEquals(2, resultado.toHoursPart());
        assertEquals(30, resultado.toMinutesPart());
    }

    @Test
    void calcularTempoMedioExecucaoServicos_shouldReturnAverageAcrossMultipleServicos() {
        var base = LocalDateTime.of(2024, 1, 15, 8, 0);
        // serviço 1: 1h = 3600s
        var s1 = new ServicoVinculado(1L, new BigDecimal("100.00"), StatusServico.FINALIZADO, base, base.plusHours(1));
        // serviço 2: 3h = 10800s
        var s2 = new ServicoVinculado(2L, new BigDecimal("200.00"), StatusServico.FINALIZADO, base, base.plusHours(3));
        // média: 7200s = 2h
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.FINALIZADA)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(s1, s2)))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(LocalDateTime.now())
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.FINALIZADA))
                .build();

        var resultado = os.calcularTempoMedioExecucaoServicos();

        assertNotNull(resultado);
        assertEquals(Duration.ofHours(2), resultado);
    }

    @Test
    void calcularTempoMedioExecucaoServicos_shouldIgnoreServicosNaoFinalizados() {
        var base = LocalDateTime.of(2024, 1, 15, 8, 0);
        var finalizado = new ServicoVinculado(1L, new BigDecimal("100.00"), StatusServico.FINALIZADO, base, base.plusHours(2));
        var emExecucao = new ServicoVinculado(2L, new BigDecimal("200.00"), StatusServico.EM_EXECUCAO, base, null);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(finalizado, emExecucao)))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_EXECUCAO))
                .build();

        var resultado = os.calcularTempoMedioExecucaoServicos();

        assertNotNull(resultado);
        assertEquals(Duration.ofHours(2), resultado);
    }

    @Test
    void calcularTempoMedioExecucaoServicos_shouldHandleDurationSpanningDays() {
        var inicio = LocalDateTime.of(2024, 1, 15, 9, 0);
        var fim = LocalDateTime.of(2024, 1, 16, 13, 30);
        var servico = new ServicoVinculado(1L, new BigDecimal("500.00"), StatusServico.FINALIZADO, inicio, fim);
        var os = OrdemDeServico.builder()
                .id(1L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.FINALIZADA)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(servico)))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(LocalDateTime.now())
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.FINALIZADA))
                .build();

        var resultado = os.calcularTempoMedioExecucaoServicos();

        assertNotNull(resultado);
        assertEquals(1, resultado.toDaysPart());
        assertEquals(4, resultado.toHoursPart());
        assertEquals(30, resultado.toMinutesPart());
    }
}
