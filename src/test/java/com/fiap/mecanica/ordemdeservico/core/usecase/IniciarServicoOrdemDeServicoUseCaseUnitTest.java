package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.*;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.IniciarServicoOrdemDeServicoUseCase;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class IniciarServicoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private IniciarServicoOrdemDeServicoUseCase iniciarServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private ServicoGateway servicoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long SERVICO_ID = 10L;
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmExecucao(StatusServico statusServico) {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_EXECUCAO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(SERVICO_ID, BigDecimal.TEN, statusServico, null, null))))
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

    private void stubOrdem(OrdemDeServico os) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(os));
    }

    private void stubServico() {
        Mockito.when(servicoGateway.buscarPorId(SERVICO_ID))
                .thenReturn(Optional.of(Servico.reconstituir(SERVICO_ID, "Troca de óleo", "desc", BigDecimal.TEN)));
    }

    @Test
    void shouldIniciarServicoSuccessfully() {
        stubOrdem(ordemEmExecucao(StatusServico.NAO_INICIADO));
        stubServico();

        var captor = ArgumentCaptor.forClass(LocalDateTime.class);

        iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID);

        Mockito.verify(ordemDeServicoGateway).atualizarServico(
                Mockito.eq(ORDEM_ID),
                Mockito.eq(SERVICO_ID),
                Mockito.eq(StatusServico.EM_EXECUCAO),
                captor.capture(),
                Mockito.isNull()
        );
        assertNotNull(captor.getValue());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verifyNoInteractions(servicoGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNotFound() {
        stubOrdem(ordemEmExecucao(StatusServico.NAO_INICIADO));
        Mockito.when(servicoGateway.buscarPorId(SERVICO_ID)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemNaoEmExecucao() {
        var ordemRecebida = OrdemDeServico.builder()
                .id(ORDEM_ID)
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
        stubOrdem(ordemRecebida);
        stubServico();

        assertThrows(IniciarServicoNaoAutorizadoException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNaoVinculado() {
        var ordemSemServico = OrdemDeServico.builder()
                .id(ORDEM_ID)
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
        stubOrdem(ordemSemServico);
        stubServico();

        assertThrows(ServicoNaoVinculadoException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoJaIniciado() {
        stubOrdem(ordemEmExecucao(StatusServico.EM_EXECUCAO));
        stubServico();

        assertThrows(ServicoEmExecucaoOuFinalizadoException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}
