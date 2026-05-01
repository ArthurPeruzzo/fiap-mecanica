package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.PecaVinculada;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.ServicoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoPecaNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.VincularPecaOrdemDeServicoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class VincularPecaOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private VincularPecaOrdemDeServicoUseCase vincularPecaOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private PecaGateway pecaGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long PECA_ID = 20L;
    private static final Integer QUANTIDADE = 2;
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnostico(List<PecaVinculada> pecasVinculadas) {
        return OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), pecasVinculadas, List.of(), null, null, null, null);
    }

    private Peca pecaComEstoque(Integer estoque) {
        return Peca.reconstituir(PECA_ID, "Filtro de óleo", "Filtro original", BigDecimal.TEN, estoque);
    }

    private void stubOrdem(OrdemDeServico os) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(os));
    }

    private void stubPeca(Peca peca) {
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.of(peca));
    }

    @Test
    void shouldVincularPecaSuccessfully() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        stubPeca(pecaComEstoque(10));

        vincularPecaOrdemDeServicoUseCase.vincular(ORDEM_ID, PECA_ID, QUANTIDADE);

        Mockito.verify(pecaGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarPeca(ORDEM_ID, PECA_ID, QUANTIDADE);
    }

    @Test
    void shouldSomarQuantidadeWhenPecaAlreadyLinked() {
        BigDecimal preco = new BigDecimal(10);
        stubOrdem(ordemEmDiagnostico(List.of(new PecaVinculada(PECA_ID, 3, preco))));
        stubPeca(pecaComEstoque(10));

        vincularPecaOrdemDeServicoUseCase.vincular(ORDEM_ID, PECA_ID, QUANTIDADE);

        Mockito.verify(pecaGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarPeca(ORDEM_ID, PECA_ID, QUANTIDADE);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> vincularPecaOrdemDeServicoUseCase.vincular(ORDEM_ID, PECA_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(pecaGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarPeca(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenPecaNotFound() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.empty());

        assertThrows(PecaNaoEncontradaException.class,
                () -> vincularPecaOrdemDeServicoUseCase.vincular(ORDEM_ID, PECA_ID, QUANTIDADE));

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarPeca(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenEstoqueInsuficiente() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        stubPeca(pecaComEstoque(0));

        assertThrows(EstoqueInsuficienteException.class,
                () -> vincularPecaOrdemDeServicoUseCase.vincular(ORDEM_ID, PECA_ID, QUANTIDADE));

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarPeca(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemNotEmDiagnostico() {
        var ordemRecebida = OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null,
                null, List.of(), List.of(), List.of(), null, null, null, null);
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemRecebida));
        stubPeca(pecaComEstoque(10));

        assertThrows(VinculoPecaNaoAutorizadaException.class,
                () -> vincularPecaOrdemDeServicoUseCase.vincular(ORDEM_ID, PECA_ID, QUANTIDADE));

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarPeca(Mockito.any(), Mockito.any(), Mockito.any());
    }
}
