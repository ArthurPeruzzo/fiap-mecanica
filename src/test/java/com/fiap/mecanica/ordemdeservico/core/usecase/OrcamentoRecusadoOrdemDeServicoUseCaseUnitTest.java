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
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.OrcamentoRecusadoOrdemDeServicoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrcamentoRecusadoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private OrcamentoRecusadoOrdemDeServicoUseCase orcamentoRecusadoOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private PecaGateway pecaGateway;

    @Mock
    private InsumoGateway insumoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 10L;
    private static final Long PECA_ID_1 = 20L;
    private static final Long PECA_ID_2 = 21L;
    private static final Long INSUMO_ID_1 = 30L;
    private static final Long INSUMO_ID_2 = 31L;

    private OrdemDeServico ordemAguardandoAprovacao() {
        return OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 2L, 3L, 5L,
                StatusOrdemDeServico.AGUARDANDO_APROVACAO, "Barulho ao frear",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ServicoVinculado(10L, new BigDecimal("150.00"), StatusServico.NAO_INICIADO, null, null)),
                List.of(
                        new PecaVinculada(PECA_ID_1, 3, new BigDecimal("45.00")),
                        new PecaVinculada(PECA_ID_2, 2, new BigDecimal("30.00"))
                ),
                List.of(
                        new InsumoVinculado(INSUMO_ID_1, 4, new BigDecimal("35.00")),
                        new InsumoVinculado(INSUMO_ID_2, 1, new BigDecimal("20.00"))
                ),
                new Orcamento(new BigDecimal("500.00")),
                LocalDateTime.now(), null, null);
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

        orcamentoRecusadoOrdemDeServicoUseCase.recursar(ORDEM_ID);

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
                () -> orcamentoRecusadoOrdemDeServicoUseCase.recursar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(pecaGateway);
        Mockito.verifyNoInteractions(insumoGateway);
    }

    @Test
    void shouldThrowWhenStatusInvalido() {
        var ordemEmDiagnostico = OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, "Barulho ao frear",
                LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(), null, null, null, null);
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemEmDiagnostico));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> orcamentoRecusadoOrdemDeServicoUseCase.recursar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(pecaGateway);
        Mockito.verifyNoInteractions(insumoGateway);
    }
}
