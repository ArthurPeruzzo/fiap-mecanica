package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.PagamentoComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.OrcamentoAprovadoViaAtendenteUseCase;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
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
class OrcamentoAprovadoViaAtendenteUseCaseUnitTest {

    @InjectMocks
    private OrcamentoAprovadoViaAtendenteUseCase orcamentoAprovadoViaAtendenteUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private PagamentoComandosGateway pagamentoComandosGateway;

    @Mock
    private TransacaoGateway transacaoGateway;

    @Mock
    private NotificacaoGateway notificacaoGateway;

    @BeforeEach
    void executarTransacaoNaMesmaThread() {
        Mockito.lenient().doAnswer(i -> {
            ((Runnable) i.getArgument(0)).run();
            return null;
        }).when(transacaoGateway).executar(Mockito.any());
    }

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 10L;

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
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
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
    void shouldAprovarOrcamentoSuccessfully() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));

        orcamentoAprovadoViaAtendenteUseCase.aprovar(ORDEM_ID);

        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(captor.capture());
        var os = captor.getValue();
        assertEquals(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO, os.getStatus());
        assertNotNull(os.getDataAprovacao());
        Mockito.verify(pagamentoComandosGateway).solicitarPagamento(ORDEM_ID, CLIENTE_ID, new BigDecimal("150.00"));
        Mockito.verify(notificacaoGateway).enviar(Mockito.any());
    }

    @Test
    void shouldPropagarErroENaoNotificarQuandoGravacaoFalha() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));
        Mockito.doThrow(new ErroAcessoBaseDeDadosException()).when(ordemDeServicoGateway).atualizar(Mockito.any());

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> orcamentoAprovadoViaAtendenteUseCase.aprovar(ORDEM_ID));

        Mockito.verify(pagamentoComandosGateway, Mockito.never()).solicitarPagamento(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldPermitirRepetirAprovacaoDepoisDeFalhaNaGravacao() {
        // A transação desfez tudo: a OS segue AGUARDANDO_APROVACAO no banco e nenhum pedido de pagamento ficou na outbox.
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenAnswer(i -> Optional.of(ordemAguardandoAprovacao()));
        Mockito.doThrow(new ErroAcessoBaseDeDadosException()).doNothing()
                .when(ordemDeServicoGateway).atualizar(Mockito.any());

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> orcamentoAprovadoViaAtendenteUseCase.aprovar(ORDEM_ID));
        orcamentoAprovadoViaAtendenteUseCase.aprovar(ORDEM_ID);

        Mockito.verify(pagamentoComandosGateway, Mockito.times(1)).solicitarPagamento(ORDEM_ID, CLIENTE_ID, new BigDecimal("150.00"));
        Mockito.verify(ordemDeServicoGateway, Mockito.times(2)).atualizar(Mockito.any());
        Mockito.verify(notificacaoGateway, Mockito.times(1)).enviar(Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> orcamentoAprovadoViaAtendenteUseCase.aprovar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
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
                () -> orcamentoAprovadoViaAtendenteUseCase.aprovar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(pagamentoComandosGateway);
    }
}
