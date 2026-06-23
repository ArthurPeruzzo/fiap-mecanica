package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.OrcamentoRecusadoViaAtendenteUseCase;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
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
    private PecaGateway pecaGateway;

    @Mock
    private InsumoGateway insumoGateway;

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

    private Peca pecaComEstoque(Long id, int estoque) {
        return Peca.reconstituir(id, "Peça", "desc", new BigDecimal("45.00"), estoque);
    }

    private Insumo insumoComEstoque(Long id, int estoque) {
        return Insumo.reconstituir(id, "Insumo", "desc", new BigDecimal("35.00"), UnidadeMedida.LITRO, estoque);
    }

    @Test
    void shouldRecusarOrcamentoSuccessfully() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));
        Mockito.when(pecaGateway.buscarPorId(PECA_ID_1)).thenReturn(Optional.of(pecaComEstoque(PECA_ID_1, 7)));
        Mockito.when(pecaGateway.buscarPorId(PECA_ID_2)).thenReturn(Optional.of(pecaComEstoque(PECA_ID_2, 8)));
        Mockito.when(insumoGateway.buscarPorId(INSUMO_ID_1)).thenReturn(Optional.of(insumoComEstoque(INSUMO_ID_1, 6)));
        Mockito.when(insumoGateway.buscarPorId(INSUMO_ID_2)).thenReturn(Optional.of(insumoComEstoque(INSUMO_ID_2, 9)));

        orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID);

        var ordemCaptor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(ordemCaptor.capture());
        var os = ordemCaptor.getValue();
        assertEquals(StatusOrdemDeServico.CANCELADA, os.getStatus());
        assertNotNull(os.getDataCancelamento());

        Mockito.verify(pecaGateway, Mockito.times(2)).atualizar(Mockito.any());
        Mockito.verify(insumoGateway, Mockito.times(2)).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> orcamentoRecusadoViaAtendenteUseCase.recusar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(pecaGateway);
        Mockito.verifyNoInteractions(insumoGateway);
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
        Mockito.verifyNoInteractions(pecaGateway);
        Mockito.verifyNoInteractions(insumoGateway);
    }
}
