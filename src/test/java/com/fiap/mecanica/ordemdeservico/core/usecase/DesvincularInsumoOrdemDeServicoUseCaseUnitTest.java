package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoEncontradoNoCatalogoException;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.InsumoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServicoStateFactory;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.DesvincularInsumoNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.QuantidadeDesvincularInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.DesvincularInsumoOrdemDeServicoUseCase;
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
class DesvincularInsumoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private DesvincularInsumoOrdemDeServicoUseCase desvincularInsumoOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private CatalogoEstoqueGateway catalogoEstoqueGateway;

    @Mock
    private ReservaEstoqueGateway reservaEstoqueGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long INSUMO_ID = 30L;
    private static final Integer QUANTIDADE = 2;
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnosticoComInsumo(Integer quantidadeVinculada) {
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
                .insumosVinculados(new ArrayList<>(List.of(new InsumoVinculado(INSUMO_ID, quantidadeVinculada, BigDecimal.TEN))))
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
    void shouldDesvincularInsumoSuccessfully() {
        stubOrdem(ordemEmDiagnosticoComInsumo(5));
        stubInsumo(insumoNoCatalogo());

        desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE);

        Mockito.verify(reservaEstoqueGateway).reservarItens(ORDEM_ID, List.of(ItemReserva.insumo(INSUMO_ID, 3)));
        Mockito.verify(ordemDeServicoGateway).desvincularOuSubtrairInsumo(ORDEM_ID, INSUMO_ID, QUANTIDADE);
    }

    @Test
    void shouldDesvincularIntegralmenteWhenQuantidadeIgualAVinculada() {
        stubOrdem(ordemEmDiagnosticoComInsumo(QUANTIDADE));
        stubInsumo(insumoNoCatalogo());

        desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE);

        Mockito.verify(reservaEstoqueGateway).reservarItens(ORDEM_ID, List.of(ItemReserva.insumo(INSUMO_ID, 0)));
        Mockito.verify(ordemDeServicoGateway).desvincularOuSubtrairInsumo(ORDEM_ID, INSUMO_ID, QUANTIDADE);
    }

    @Test
    void shouldRestaurarReservaAnteriorQuandoGravacaoDoDesvinculoFalhaDepoisDeReservar() {
        stubOrdem(ordemEmDiagnosticoComInsumo(5));
        stubInsumo(insumoNoCatalogo());
        Mockito.doThrow(new ErroAcessoBaseDeDadosException()).when(ordemDeServicoGateway)
                .desvincularOuSubtrairInsumo(Mockito.any(), Mockito.any(), Mockito.any());

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        var ordem = Mockito.inOrder(reservaEstoqueGateway);
        ordem.verify(reservaEstoqueGateway).reservarItens(ORDEM_ID, List.of(ItemReserva.insumo(INSUMO_ID, 3)));
        ordem.verify(reservaEstoqueGateway).reservarItens(ORDEM_ID, List.of(ItemReserva.insumo(INSUMO_ID, 5)));
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(catalogoEstoqueGateway, reservaEstoqueGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenInsumoNotFound() {
        stubOrdem(ordemEmDiagnosticoComInsumo(5));
        Mockito.when(catalogoEstoqueGateway.buscarInsumo(INSUMO_ID)).thenReturn(Optional.empty());

        assertThrows(InsumoNaoEncontradoNoCatalogoException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(reservaEstoqueGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemNotEmDiagnostico() {
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
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemRecebida));
        stubInsumo(insumoNoCatalogo());

        assertThrows(DesvincularInsumoNaoAutorizadaException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(reservaEstoqueGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenInsumoNaoVinculado() {
        var ordemSemInsumo = OrdemDeServico.builder()
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
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemSemInsumo));
        stubInsumo(insumoNoCatalogo());

        assertThrows(InsumoNaoVinculadoException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(reservaEstoqueGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenQuantidadeMaiorQueVinculada() {
        stubOrdem(ordemEmDiagnosticoComInsumo(1));
        stubInsumo(insumoNoCatalogo());

        assertThrows(QuantidadeDesvincularInvalidaException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, 5));

        Mockito.verifyNoInteractions(reservaEstoqueGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }
}
