package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueInsuficienteParaOrdemException;
import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoEncontradoNoCatalogoException;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.InsumoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServicoStateFactory;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoInsumoNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.VincularInsumoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class VincularInsumoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private VincularInsumoOrdemDeServicoUseCase vincularInsumoOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private CatalogoEstoqueGateway catalogoEstoqueGateway;

    @Mock
    private ReservaEstoqueGateway reservaEstoqueGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long INSUMO_ID = 30L;
    private static final Integer QUANTIDADE = 3;
    private static final BigDecimal PRECO = BigDecimal.TEN;
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnostico(List<InsumoVinculado> insumosVinculados) {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
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
                .insumosVinculados(new ArrayList<>(insumosVinculados))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();
    }

    private ItemCatalogo insumoNoCatalogo() {
        return new ItemCatalogo(INSUMO_ID, BigDecimal.TEN);
    }

    private void stubOrdem(OrdemDeServico os) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(os));
    }

    private void stubInsumo(ItemCatalogo insumo) {
        Mockito.when(catalogoEstoqueGateway.buscarInsumo(INSUMO_ID)).thenReturn(Optional.of(insumo));
    }

    @Test
    void shouldVincularInsumoSuccessfully() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        stubInsumo(insumoNoCatalogo());

        vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE);

        Mockito.verify(reservaEstoqueGateway).reservarItens(ORDEM_ID, List.of(ItemReserva.insumo(INSUMO_ID, QUANTIDADE)));
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarInsumo(ORDEM_ID, INSUMO_ID, QUANTIDADE, PRECO);
    }

    @Test
    void shouldSomarQuantidadeWhenInsumoAlreadyLinked() {
        stubOrdem(ordemEmDiagnostico(List.of(new InsumoVinculado(INSUMO_ID, 5, BigDecimal.TEN))));
        stubInsumo(insumoNoCatalogo());

        vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE);

        Mockito.verify(reservaEstoqueGateway).reservarItens(ORDEM_ID, List.of(ItemReserva.insumo(INSUMO_ID, 8)));
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarInsumo(ORDEM_ID, INSUMO_ID, QUANTIDADE, PRECO);
    }

    @Test
    void shouldRestaurarReservaAnteriorQuandoGravacaoDoVinculoFalhaDepoisDeReservar() {
        stubOrdem(ordemEmDiagnostico(List.of(new InsumoVinculado(INSUMO_ID, 4, PRECO))));
        stubInsumo(insumoNoCatalogo());
        Mockito.doThrow(new ErroAcessoBaseDeDadosException()).when(ordemDeServicoGateway)
                .vincularOuSomarInsumo(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        var ordem = Mockito.inOrder(reservaEstoqueGateway);
        ordem.verify(reservaEstoqueGateway).reservarItens(ORDEM_ID, List.of(ItemReserva.insumo(INSUMO_ID, 7)));
        ordem.verify(reservaEstoqueGateway).reservarItens(ORDEM_ID, List.of(ItemReserva.insumo(INSUMO_ID, 4)));
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(catalogoEstoqueGateway, reservaEstoqueGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarInsumo(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenInsumoNotFound() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        Mockito.when(catalogoEstoqueGateway.buscarInsumo(INSUMO_ID)).thenReturn(Optional.empty());

        assertThrows(InsumoNaoEncontradoNoCatalogoException.class,
                () -> vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(reservaEstoqueGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarInsumo(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenEstoqueInsuficiente() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        stubInsumo(insumoNoCatalogo());
        Mockito.doThrow(new EstoqueInsuficienteParaOrdemException())
                .when(reservaEstoqueGateway).reservarItens(ORDEM_ID, List.of(ItemReserva.insumo(INSUMO_ID, QUANTIDADE)));

        assertThrows(EstoqueInsuficienteParaOrdemException.class,
                () -> vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarInsumo(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemStatusNotAllowedForVincularInsumo() {
        var ordemDiagnosticoConcluido = OrdemDeServico.builder()
                .id(ORDEM_ID)
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
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemDiagnosticoConcluido));
        stubInsumo(insumoNoCatalogo());

        assertThrows(VinculoInsumoNaoAutorizadaException.class,
                () -> vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(reservaEstoqueGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarInsumo(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}
