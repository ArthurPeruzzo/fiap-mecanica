package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServicoStateFactory;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ConsultarStatusOrdemDeServicoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConsultarStatusOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private ConsultarStatusOrdemDeServicoUseCase consultarStatusOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    private static final Long ORDEM_ID = 1L;

    private OrdemDeServico ordemComStatus(StatusOrdemDeServico status) {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(2L)
                .veiculoId(3L)
                .atendenteId(4L)
                .mecanicoId(null)
                .status(status)
                .state(OrdemDeServicoStateFactory.from(status))
                .build();
    }

    @ParameterizedTest
    @EnumSource(StatusOrdemDeServico.class)
    void shouldReturnCorrectStatusForAllPossibleStatuses(StatusOrdemDeServico status) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID))
                .thenReturn(Optional.of(ordemComStatus(status)));

        var dto = consultarStatusOrdemDeServicoUseCase.consultar(ORDEM_ID);

        assertEquals(ORDEM_ID, dto.id());
        assertEquals(status.name(), dto.status());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> consultarStatusOrdemDeServicoUseCase.consultar(ORDEM_ID));
    }
}
