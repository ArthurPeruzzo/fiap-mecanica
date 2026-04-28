package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.InsumoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoInsumoNaoAutorizadaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.VincularInsumoOrdemDeServicoUseCase;
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
class VincularInsumoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private VincularInsumoOrdemDeServicoUseCase vincularInsumoOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private InsumoGateway insumoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long INSUMO_ID = 30L;
    private static final Integer QUANTIDADE = 3;
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnostico(List<InsumoVinculado> insumosVinculados) {
        return OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), insumosVinculados);
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
    void shouldVincularInsumoSuccessfully() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        stubInsumo(insumoComEstoque(10));

        vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE);

        Mockito.verify(insumoGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarInsumo(ORDEM_ID, INSUMO_ID, QUANTIDADE);
    }

    @Test
    void shouldSomarQuantidadeWhenInsumoAlreadyLinked() {
        stubOrdem(ordemEmDiagnostico(List.of(new InsumoVinculado(INSUMO_ID, 5))));
        stubInsumo(insumoComEstoque(10));

        vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE);

        Mockito.verify(insumoGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarInsumo(ORDEM_ID, INSUMO_ID, QUANTIDADE);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verifyNoInteractions(insumoGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenInsumoNotFound() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        Mockito.when(insumoGateway.buscarPorId(INSUMO_ID)).thenReturn(Optional.empty());

        assertThrows(InsumoNaoEncontradoException.class,
                () -> vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verify(insumoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenEstoqueInsuficiente() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        stubInsumo(insumoComEstoque(0));

        assertThrows(EstoqueInsuficienteException.class,
                () -> vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verify(insumoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemNotEmDiagnostico() {
        var ordemRecebida = OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null, List.of(), List.of(), List.of());
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemRecebida));
        stubInsumo(insumoComEstoque(10));

        assertThrows(VinculoInsumoNaoAutorizadaException.class,
                () -> vincularInsumoOrdemDeServicoUseCase.vincular(ORDEM_ID, INSUMO_ID, QUANTIDADE));

        Mockito.verify(insumoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularOuSomarInsumo(Mockito.any(), Mockito.any(), Mockito.any());
    }
}
