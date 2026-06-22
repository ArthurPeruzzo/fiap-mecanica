package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.LinkAprovacaoOrcamentoInvalidoException;
import com.fiap.mecanica.ordemdeservico.core.exception.LinkAprovacaoOrcamentoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.OrcamentoAprovadoViaTokenUseCase;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrcamentoAprovadoViaTokenUseCaseUnitTest {

    @InjectMocks
    private OrcamentoAprovadoViaTokenUseCase orcamentoAprovadoViaTokenUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 10L;
    private static final String TOKEN = UUID.randomUUID().toString();

    private LinkAprovacaoOrcamento linkValido() {
        return LinkAprovacaoOrcamento.builder()
                .id(1L)
                .ordemDeServicoId(ORDEM_ID)
                .token(TOKEN)
                .dataExpiracao(LocalDateTime.now().plusDays(3))
                .dataUtilizacao(null)
                .build();
    }

    private LinkAprovacaoOrcamento linkExpirado() {
        return LinkAprovacaoOrcamento.builder()
                .id(1L)
                .ordemDeServicoId(ORDEM_ID)
                .token(TOKEN)
                .dataExpiracao(LocalDateTime.now().minusDays(1))
                .dataUtilizacao(null)
                .build();
    }

    private LinkAprovacaoOrcamento linkJaUtilizado() {
        return LinkAprovacaoOrcamento.builder()
                .id(1L)
                .ordemDeServicoId(ORDEM_ID)
                .token(TOKEN)
                .dataExpiracao(LocalDateTime.now().plusDays(3))
                .dataUtilizacao(LocalDateTime.now().minusHours(1))
                .build();
    }

    private OrdemDeServico ordemAguardandoAprovacao() {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(CLIENTE_ID)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.AGUARDANDO_APROVACAO)
                .descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(
                        new ServicoVinculado(10L, new BigDecimal("150.00"), StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>())
                .insumosVinculados(new ArrayList<>())
                .orcamento(new Orcamento(new BigDecimal("150.00")))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.AGUARDANDO_APROVACAO))
                .build();
    }

    @Test
    void shouldAprovarOrcamentoViaTokenSuccessfully() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkValido()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));

        orcamentoAprovadoViaTokenUseCase.aprovar(TOKEN);

        var ordemCaptor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(ordemCaptor.capture());
        assertEquals(StatusOrdemDeServico.EM_EXECUCAO, ordemCaptor.getValue().getStatus());
        assertNotNull(ordemCaptor.getValue().getDataAprovacao());

        var linkCaptor = ArgumentCaptor.forClass(LinkAprovacaoOrcamento.class);
        Mockito.verify(linkAprovacaoOrcamentoGateway).atualizar(linkCaptor.capture());
        assertNotNull(linkCaptor.getValue().getDataUtilizacao());
    }

    @Test
    void shouldThrowWhenTokenNaoEncontrado() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.empty());

        assertThrows(LinkAprovacaoOrcamentoNaoEncontradoException.class,
                () -> orcamentoAprovadoViaTokenUseCase.aprovar(TOKEN));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
    }

    @Test
    void shouldThrowWhenTokenExpirado() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkExpirado()));

        assertThrows(LinkAprovacaoOrcamentoInvalidoException.class,
                () -> orcamentoAprovadoViaTokenUseCase.aprovar(TOKEN));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
        Mockito.verify(linkAprovacaoOrcamentoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenTokenJaUtilizado() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkJaUtilizado()));

        assertThrows(LinkAprovacaoOrcamentoInvalidoException.class,
                () -> orcamentoAprovadoViaTokenUseCase.aprovar(TOKEN));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
        Mockito.verify(linkAprovacaoOrcamentoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkValido()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> orcamentoAprovadoViaTokenUseCase.aprovar(TOKEN));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(linkAprovacaoOrcamentoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenStatusInvalido() {
        var ordemEmDiagnostico = OrdemDeServico.builder()
                .id(ORDEM_ID).clienteId(CLIENTE_ID).veiculoId(2L).atendenteId(3L).mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO).descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now()).dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>()).pecasVinculadas(new ArrayList<>())
                .insumosVinculados(new ArrayList<>()).orcamento(null)
                .dataEnvioOrcamento(null).dataCancelamento(null).dataAprovacao(null)
                .dataFinalizacao(null).dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkValido()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemEmDiagnostico));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> orcamentoAprovadoViaTokenUseCase.aprovar(TOKEN));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(linkAprovacaoOrcamentoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldNotAtualizarLinkWhenAprovarFails() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkValido()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));
        Mockito.doThrow(new RuntimeException("db error")).when(ordemDeServicoGateway).atualizar(Mockito.any());

        assertThrows(RuntimeException.class,
                () -> orcamentoAprovadoViaTokenUseCase.aprovar(TOKEN));

        Mockito.verify(linkAprovacaoOrcamentoGateway, Mockito.never()).atualizar(Mockito.any());
    }
}
