package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServicoStateFactory;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.ServicoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.MecanicoNaoResponsavelPelaOrdemDeServicoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoSemServicosException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ConcluirDiagnosticoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConcluirDiagnosticoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private ConcluirDiagnosticoOrdemDeServicoUseCase concluirDiagnosticoOrdemDeServicoUseCase;

    @Mock
    private MecanicoGateway mecanicoGateway;

    @Mock
    private TokenGateway tokenGateway;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    private static final Long USER_ID = 10L;
    private static final Long MECANICO_ID = 5L;
    private static final Long ORDEM_ID = 1L;

    private void stubMecanico() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID))
                .thenReturn(Optional.of(Mecanico.builder().id(MECANICO_ID).build()));
    }

    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnostico() {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(MECANICO_ID)
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
    }

    private OrdemDeServico ordemEmDiagnosticoSemServicos() {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(MECANICO_ID)
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
    void shouldConcluirDiagnostico() {
        stubMecanico();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemEmDiagnostico()));

        concluirDiagnosticoOrdemDeServicoUseCase.concluirDiagnostico(ORDEM_ID);

        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(captor.capture());
        var os = captor.getValue();
        assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, os.getStatus());
        assertNotNull(os.getDataConclusaoDiagnostico());
        assertNotNull(os.getOrcamento());
    }

    @Test
    void shouldThrowWhenMecanicoNotFound() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(MecanicoNaoEncontradoException.class,
                () -> concluirDiagnosticoOrdemDeServicoUseCase.concluirDiagnostico(ORDEM_ID));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        stubMecanico();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> concluirDiagnosticoOrdemDeServicoUseCase.concluirDiagnostico(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenMecanicoNaoEhResponsavel() {
        stubMecanico();
        var ordemDeOutroMecanico = OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(99L)
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
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemDeOutroMecanico));

        assertThrows(MecanicoNaoResponsavelPelaOrdemDeServicoException.class,
                () -> concluirDiagnosticoOrdemDeServicoUseCase.concluirDiagnostico(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenSemServicosVinculados() {
        stubMecanico();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemEmDiagnosticoSemServicos()));

        assertThrows(OrdemDeServicoSemServicosException.class,
                () -> concluirDiagnosticoOrdemDeServicoUseCase.concluirDiagnostico(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenTransicaoDeStatusInvalida() {
        stubMecanico();
        var ordemConcluida = OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(MECANICO_ID)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(1L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null))))
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
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemConcluida));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> concluirDiagnosticoOrdemDeServicoUseCase.concluirDiagnostico(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }
}
