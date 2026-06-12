package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServicoStateFactory;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.PecaVinculada;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.DesvincularPecaNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.PecaNaoVinculadaException;
import com.fiap.mecanica.ordemdeservico.core.exception.QuantidadeDesvincularInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.DesvincularPecaOrdemDeServicoUseCase;
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
class DesvincularPecaOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private DesvincularPecaOrdemDeServicoUseCase desvincularPecaOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private PecaGateway pecaGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long PECA_ID = 20L;
    private static final Integer QUANTIDADE = 2;
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnosticoComPeca(Integer quantidadeVinculada) {
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
                .pecasVinculadas(new ArrayList<>(List.of(new PecaVinculada(PECA_ID, quantidadeVinculada, new BigDecimal(10)))))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();
    }

    private Peca pecaComEstoque(Integer estoque) {
        return Peca.reconstituir(PECA_ID, "Pastilha de freio", "Pastilha dianteira", BigDecimal.TEN, estoque);
    }

    private void stubOrdem(OrdemDeServico os) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(os));
    }

    private void stubPeca(Peca peca) {
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.of(peca));
    }

    @Test
    void shouldDesvincularPecaSuccessfully() {
        stubOrdem(ordemEmDiagnosticoComPeca(5));
        stubPeca(pecaComEstoque(3));

        desvincularPecaOrdemDeServicoUseCase.desvincular(ORDEM_ID, PECA_ID, QUANTIDADE);

        Mockito.verify(pecaGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).desvincularOuSubtrairPeca(ORDEM_ID, PECA_ID, QUANTIDADE);
    }

    @Test
    void shouldDesvincularIntegralmenteWhenQuantidadeIgualAVinculada() {
        stubOrdem(ordemEmDiagnosticoComPeca(QUANTIDADE));
        stubPeca(pecaComEstoque(0));

        desvincularPecaOrdemDeServicoUseCase.desvincular(ORDEM_ID, PECA_ID, QUANTIDADE);

        Mockito.verify(pecaGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).desvincularOuSubtrairPeca(ORDEM_ID, PECA_ID, QUANTIDADE);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> desvincularPecaOrdemDeServicoUseCase.desvincular(ORDEM_ID, PECA_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(pecaGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairPeca(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenPecaNotFound() {
        stubOrdem(ordemEmDiagnosticoComPeca(5));
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.empty());

        assertThrows(PecaNaoEncontradaException.class,
                () -> desvincularPecaOrdemDeServicoUseCase.desvincular(ORDEM_ID, PECA_ID, QUANTIDADE));

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairPeca(Mockito.any(), Mockito.any(), Mockito.any());
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
        stubPeca(pecaComEstoque(3));

        assertThrows(DesvincularPecaNaoAutorizadaException.class,
                () -> desvincularPecaOrdemDeServicoUseCase.desvincular(ORDEM_ID, PECA_ID, QUANTIDADE));

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairPeca(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenPecaNaoVinculada() {
        var ordemSemPeca = OrdemDeServico.builder()
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
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemSemPeca));
        stubPeca(pecaComEstoque(3));

        assertThrows(PecaNaoVinculadaException.class,
                () -> desvincularPecaOrdemDeServicoUseCase.desvincular(ORDEM_ID, PECA_ID, QUANTIDADE));

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairPeca(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenQuantidadeMaiorQueVinculada() {
        stubOrdem(ordemEmDiagnosticoComPeca(1));
        stubPeca(pecaComEstoque(3));

        assertThrows(QuantidadeDesvincularInvalidaException.class,
                () -> desvincularPecaOrdemDeServicoUseCase.desvincular(ORDEM_ID, PECA_ID, 5));

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularOuSubtrairPeca(Mockito.any(), Mockito.any(), Mockito.any());
    }
}
