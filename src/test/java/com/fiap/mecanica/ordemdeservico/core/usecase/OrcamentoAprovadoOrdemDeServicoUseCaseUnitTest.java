package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.OrcamentoAprovadoOrdemDeServicoUseCase;
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
class OrcamentoAprovadoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private OrcamentoAprovadoOrdemDeServicoUseCase orcamentoAprovadoOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 10L;

    private OrdemDeServico ordemAguardandoAprovacao() {
        return OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 2L, 3L, 5L,
                StatusOrdemDeServico.AGUARDANDO_APROVACAO, "Barulho ao frear",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ServicoVinculado(10L, new BigDecimal("150.00"), StatusServico.NAO_INICIADO, null, null)),
                List.of(), List.of(),
                new Orcamento(new BigDecimal("150.00")),
                LocalDateTime.now(), null, null, null, null);
    }

    @Test
    void shouldAprovarOrcamentoSuccessfully() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));

        orcamentoAprovadoOrdemDeServicoUseCase.aprovar(ORDEM_ID);

        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(captor.capture());
        var os = captor.getValue();
        assertEquals(StatusOrdemDeServico.EM_EXECUCAO, os.getStatus());
        assertNotNull(os.getDataAprovacao());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> orcamentoAprovadoOrdemDeServicoUseCase.aprovar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowWhenStatusInvalido() {
        var ordemEmDiagnostico = OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, "Barulho ao frear",
                LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(), null, null, null, null, null, null);
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemEmDiagnostico));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> orcamentoAprovadoOrdemDeServicoUseCase.aprovar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }
}
