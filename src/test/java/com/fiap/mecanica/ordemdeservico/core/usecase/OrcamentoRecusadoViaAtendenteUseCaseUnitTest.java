package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.OrcamentoRecusadoViaAtendenteUseCase;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.BeforeEach;
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
class OrcamentoRecusadoViaAtendenteUseCaseUnitTest {

    @InjectMocks
    private OrcamentoRecusadoViaAtendenteUseCase orcamentoRecusadoViaAtendenteUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private EstoqueComandosGateway estoqueComandosGateway;

    @Mock
    private TransacaoGateway transacaoGateway;

    @BeforeEach
    void executarTransacaoNaMesmaThread() {
        Mockito.lenient().doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(transacaoGateway).executar(Mockito.any());
    }

    @Mock
    private NotificacaoGateway notificacaoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 10L;
    private static final Long PECA_ID_1 = 20L;
    private static final Long PECA_ID_2 = 21L;
    private static final Long INSUMO_ID_1 = 30L;
    private static final Long INSUMO_ID_2 = 31L;

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
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, new BigDecimal("150.00"), StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of(
                        new PecaVinculada(PECA_ID_1, 3, new BigDecimal("45.00")),
                        new PecaVinculada(PECA_ID_2, 2, new BigDecimal("30.00"))
                )))
                .insumosVinculados(new ArrayList<>(List.of(
                        new InsumoVinculado(INSUMO_ID_1, 4, new BigDecimal("35.00")),
                        new InsumoVinculado(INSUMO_ID_2, 1, new BigDecimal("20.00"))
                )))
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
    void shouldRecusarOrcamentoSuccessfully() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));

        orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID);

        var ordemCaptor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(ordemCaptor.capture());
        var os = ordemCaptor.getValue();
        assertEquals(StatusOrdemDeServico.CANCELADA, os.getStatus());
        assertNotNull(os.getDataCancelamento());

        Mockito.verify(estoqueComandosGateway).solicitarLiberacaoDeReservas(ORDEM_ID);
    }

    @Test
    void shouldPropagarErroENaoRegistrarComandoNemNotificarQuandoGravacaoDaOrdemFalha() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));
        Mockito.doThrow(new ErroAcessoBaseDeDadosException()).when(ordemDeServicoGateway).atualizar(Mockito.any());

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID));

        Mockito.verify(estoqueComandosGateway, Mockito.never()).solicitarLiberacaoDeReservas(Mockito.any());
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldPropagarErroENaoNotificarQuandoRegistroDoComandoFalha() {
        // Na prática a transação desfaz a gravação da OS; aqui basta garantir que o erro não é engolido.
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));
        Mockito.doThrow(new ErroAcessoBaseDeDadosException()).when(estoqueComandosGateway)
                .solicitarLiberacaoDeReservas(ORDEM_ID);

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID));

        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldGravarOrdemEComandoDentroDaMesmaTransacao() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));

        orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID);

        var ordem = Mockito.inOrder(transacaoGateway, ordemDeServicoGateway, estoqueComandosGateway, notificacaoGateway);
        ordem.verify(transacaoGateway).executar(Mockito.any());
        ordem.verify(ordemDeServicoGateway).atualizar(Mockito.any());
        ordem.verify(estoqueComandosGateway).solicitarLiberacaoDeReservas(ORDEM_ID);
        ordem.verify(notificacaoGateway).enviar(Mockito.any());
    }

    @Test
    void shouldPermitirRepetirRecusaDepoisDeFalhaNaGravacao() {
        // A OS segue AGUARDANDO_APROVACAO no banco após a falha e nenhum comando foi registrado.
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenAnswer(i -> Optional.of(ordemAguardandoAprovacao()));
        Mockito.doThrow(new ErroAcessoBaseDeDadosException()).doNothing()
                .when(ordemDeServicoGateway).atualizar(Mockito.any());

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID));
        orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID);

        Mockito.verify(estoqueComandosGateway, Mockito.times(1)).solicitarLiberacaoDeReservas(ORDEM_ID);
        Mockito.verify(ordemDeServicoGateway, Mockito.times(2)).atualizar(Mockito.any());
        Mockito.verify(notificacaoGateway, Mockito.times(1)).enviar(Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(estoqueComandosGateway);
    }

    @Test
    void shouldThrowWhenStatusInvalido() {
        var ordemEmDiagnostico = OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(CLIENTE_ID)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao("Barulho ao frear")
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
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemEmDiagnostico));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(estoqueComandosGateway);
    }
}
