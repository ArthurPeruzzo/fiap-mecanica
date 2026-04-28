package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoServicoNaoAutorizadoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.DesvincularServicoOrdemDeServicoUseCase;
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
class DesvincularServicoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private DesvincularServicoOrdemDeServicoUseCase desvincularServicoOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private ServicoGateway servicoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long SERVICO_ID = 10L;

    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmDiagnostico(List<Long> servicoIds) {
        return OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null, servicoIds, List.of());
    }

    private Servico servicoPadrao() {
        return Servico.reconstituir(SERVICO_ID, "Troca de óleo", "desc", BigDecimal.TEN);
    }

    private void stubOrdem(OrdemDeServico os) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(os));
    }

    private void stubServico() {
        Mockito.when(servicoGateway.buscarPorId(SERVICO_ID)).thenReturn(Optional.of(servicoPadrao()));
    }

    @Test
    void shouldDesvincularServicoSuccessfully() {
        stubOrdem(ordemEmDiagnostico(List.of(SERVICO_ID)));
        stubServico();

        desvincularServicoOrdemDeServicoUseCase.desvincular(ORDEM_ID, SERVICO_ID);

        Mockito.verify(ordemDeServicoGateway).desvincularServico(ORDEM_ID, SERVICO_ID);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> desvincularServicoOrdemDeServicoUseCase.desvincular(ORDEM_ID, SERVICO_ID));

        Mockito.verifyNoInteractions(servicoGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularServico(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNotFound() {
        stubOrdem(ordemEmDiagnostico(List.of(SERVICO_ID)));
        Mockito.when(servicoGateway.buscarPorId(SERVICO_ID)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class,
                () -> desvincularServicoOrdemDeServicoUseCase.desvincular(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularServico(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNotLinked() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        stubServico();

        assertThrows(ServicoNaoVinculadoException.class,
                () -> desvincularServicoOrdemDeServicoUseCase.desvincular(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularServico(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemNotEmDiagnostico() {
        var ordemDiagnosticoConcluido = OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(SERVICO_ID), List.of());
        stubOrdem(ordemDiagnosticoConcluido);
        stubServico();

        assertThrows(VinculoServicoNaoAutorizadoException.class,
                () -> desvincularServicoOrdemDeServicoUseCase.desvincular(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).desvincularServico(Mockito.any(), Mockito.any());
    }
}
