package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.InsumoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServicoStateFactory;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.DesvincularInsumoNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.QuantidadeDesvincularInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.DesvincularInsumoOrdemDeServicoUseCase;
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
    private InsumoGateway insumoGateway;

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

    private Insumo insumoComEstoque(Integer estoque) {
        return Insumo.reconstituir(INSUMO_ID, "Óleo motor 5W30", "Óleo sintético", BigDecimal.TEN,
                UnidadeMedida.LITRO, estoque);
    }

    private void stubOrdem(OrdemDeServico os) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(os));
    }

    private void stubInsumo(Insumo insumo) {
        Mockito.when(insumoGateway.buscarPorId(INSUMO_ID)).thenReturn(Optional.of(insumo));
    }

    @Test
    void shouldDesvincularInsumoSuccessfully() {
        stubOrdem(ordemEmDiagnosticoComInsumo(5));
        stubInsumo(insumoComEstoque(3));

        desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE);

        Mockito.verify(insumoGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).desvincularOuSubtrairInsumo(ORDEM_ID, INSUMO_ID, QUANTIDADE);
    }

    @Test
    void shouldDesvincularIntegralmenteWhenQuantidadeIgualAVinculada() {
        stubOrdem(ordemEmDiagnosticoComInsumo(QUANTIDADE));
        stubInsumo(insumoComEstoque(0));

        desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE);

        Mockito.verify(insumoGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).desvincularOuSubtrairInsumo(ORDEM_ID, INSUMO_ID, QUANTIDADE);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(insumoGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenInsumoNotFound() {
        stubOrdem(ordemEmDiagnosticoComInsumo(5));
        Mockito.when(insumoGateway.buscarPorId(INSUMO_ID)).thenReturn(Optional.empty());

        assertThrows(InsumoNaoEncontradoException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verify(insumoGateway, Mockito.never()).atualizar(Mockito.any());
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
        stubInsumo(insumoComEstoque(3));

        assertThrows(DesvincularInsumoNaoAutorizadaException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verify(insumoGateway, Mockito.never()).atualizar(Mockito.any());
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
        stubInsumo(insumoComEstoque(3));

        assertThrows(InsumoNaoVinculadoException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verify(insumoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenQuantidadeMaiorQueVinculada() {
        stubOrdem(ordemEmDiagnosticoComInsumo(1));
        stubInsumo(insumoComEstoque(3));

        assertThrows(QuantidadeDesvincularInvalidaException.class,
                () -> desvincularInsumoOrdemDeServicoUseCase.desvincular(ORDEM_ID, INSUMO_ID, 5));

        Mockito.verify(insumoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }
}
