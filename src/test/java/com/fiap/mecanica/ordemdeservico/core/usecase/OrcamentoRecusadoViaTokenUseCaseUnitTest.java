package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.LinkAprovacaoOrcamentoInvalidoException;
import com.fiap.mecanica.ordemdeservico.core.exception.LinkAprovacaoOrcamentoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.OrcamentoRecusadoViaTokenUseCase;
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
class OrcamentoRecusadoViaTokenUseCaseUnitTest {

    @InjectMocks
    private OrcamentoRecusadoViaTokenUseCase orcamentoRecusadoViaTokenUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private PecaGateway pecaGateway;

    @Mock
    private InsumoGateway insumoGateway;

    @Mock
    private LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 10L;
    private static final Long PECA_ID = 20L;
    private static final Long INSUMO_ID = 30L;
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
                .pecasVinculadas(new ArrayList<>(List.of(
                        new PecaVinculada(PECA_ID, 2, new BigDecimal("45.00")))))
                .insumosVinculados(new ArrayList<>(List.of(
                        new InsumoVinculado(INSUMO_ID, 3, new BigDecimal("35.00")))))
                .orcamento(new Orcamento(new BigDecimal("500.00")))
                .dataEnvioOrcamento(LocalDateTime.now())
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.AGUARDANDO_APROVACAO))
                .build();
    }

    @Test
    void shouldRecusarOrcamentoViaTokenSuccessfully() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkValido()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.of(
                Peca.reconstituir(PECA_ID, "Filtro", "desc", new BigDecimal("45.00"), 5)));
        Mockito.when(insumoGateway.buscarPorId(INSUMO_ID)).thenReturn(Optional.of(
                Insumo.reconstituir(INSUMO_ID, "Óleo", "desc", new BigDecimal("35.00"), UnidadeMedida.LITRO, 10)));

        orcamentoRecusadoViaTokenUseCase.recusar(TOKEN);

        var ordemCaptor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(ordemCaptor.capture());
        assertEquals(StatusOrdemDeServico.CANCELADA, ordemCaptor.getValue().getStatus());
        assertNotNull(ordemCaptor.getValue().getDataCancelamento());

        var linkCaptor = ArgumentCaptor.forClass(LinkAprovacaoOrcamento.class);
        Mockito.verify(linkAprovacaoOrcamentoGateway).atualizar(linkCaptor.capture());
        assertNotNull(linkCaptor.getValue().getDataUtilizacao());

        Mockito.verify(pecaGateway).atualizar(Mockito.any());
        Mockito.verify(insumoGateway).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenTokenNaoEncontrado() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.empty());

        assertThrows(LinkAprovacaoOrcamentoNaoEncontradoException.class,
                () -> orcamentoRecusadoViaTokenUseCase.recusar(TOKEN));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
        Mockito.verifyNoInteractions(pecaGateway);
        Mockito.verifyNoInteractions(insumoGateway);
    }

    @Test
    void shouldThrowWhenTokenExpirado() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkExpirado()));

        assertThrows(LinkAprovacaoOrcamentoInvalidoException.class,
                () -> orcamentoRecusadoViaTokenUseCase.recusar(TOKEN));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
        Mockito.verify(linkAprovacaoOrcamentoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenTokenJaUtilizado() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkJaUtilizado()));

        assertThrows(LinkAprovacaoOrcamentoInvalidoException.class,
                () -> orcamentoRecusadoViaTokenUseCase.recusar(TOKEN));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
        Mockito.verify(linkAprovacaoOrcamentoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkValido()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> orcamentoRecusadoViaTokenUseCase.recusar(TOKEN));

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
                () -> orcamentoRecusadoViaTokenUseCase.recusar(TOKEN));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(linkAprovacaoOrcamentoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldNotAtualizarLinkWhenRecusarFails() {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken(TOKEN)).thenReturn(Optional.of(linkValido()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.of(
                Peca.reconstituir(PECA_ID, "Filtro", "desc", new BigDecimal("45.00"), 5)));
        Mockito.when(insumoGateway.buscarPorId(INSUMO_ID)).thenReturn(Optional.of(
                Insumo.reconstituir(INSUMO_ID, "Óleo", "desc", new BigDecimal("35.00"), UnidadeMedida.LITRO, 10)));
        Mockito.doThrow(new RuntimeException("db error")).when(ordemDeServicoGateway).atualizar(Mockito.any());

        assertThrows(RuntimeException.class,
                () -> orcamentoRecusadoViaTokenUseCase.recusar(TOKEN));

        Mockito.verify(linkAprovacaoOrcamentoGateway, Mockito.never()).atualizar(Mockito.any());
    }
}
