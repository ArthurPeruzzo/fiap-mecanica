package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.Orcamento;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.ServicoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.EntregarOrdemDeServicoUseCase;
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
class EntregarOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private EntregarOrdemDeServicoUseCase entregarOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 2L;
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemFinalizada() {
        return OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 3L, 4L, 5L,
                StatusOrdemDeServico.FINALIZADA, DESCRICAO,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.FINALIZADO,
                        LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1))),
                List.of(), List.of(),
                new Orcamento(BigDecimal.TEN),
                LocalDateTime.now().minusDays(1),
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(1),
                null);
    }

    @Test
    void shouldEntregarOrdemDeServicoSuccessfully() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemFinalizada()));

        entregarOrdemDeServicoUseCase.entregar(ORDEM_ID);

        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(captor.capture());
        var os = captor.getValue();
        assertEquals(StatusOrdemDeServico.ENTREGUE, os.getStatus());
        assertNotNull(os.getDataEntrega());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> entregarOrdemDeServicoUseCase.entregar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }
}
