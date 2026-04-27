package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoJaVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoServicoNaoAutorizadoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.VincularServicoOrdemDeServicoUseCase;
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
class VincularServicoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private VincularServicoOrdemDeServicoUseCase vincularServicoOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private ServicoGateway servicoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long SERVICO_ID = 10L;

    private OrdemDeServico ordemEmDiagnostico(List<Long> servicoIds) {
        return OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, LocalDateTime.now(), LocalDateTime.now(), null, servicoIds);
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
    void shouldVincularServicoSuccessfully() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        stubServico();

        vincularServicoOrdemDeServicoUseCase.vincular(ORDEM_ID, SERVICO_ID);

        Mockito.verify(ordemDeServicoGateway).vincularServico(ORDEM_ID, SERVICO_ID);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> vincularServicoOrdemDeServicoUseCase.vincular(ORDEM_ID, SERVICO_ID));

        Mockito.verifyNoInteractions(servicoGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularServico(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNotFound() {
        stubOrdem(ordemEmDiagnostico(List.of()));
        Mockito.when(servicoGateway.buscarPorId(SERVICO_ID)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class,
                () -> vincularServicoOrdemDeServicoUseCase.vincular(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularServico(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoAlreadyLinked() {
        stubOrdem(ordemEmDiagnostico(List.of(SERVICO_ID)));
        stubServico();

        assertThrows(ServicoJaVinculadoException.class,
                () -> vincularServicoOrdemDeServicoUseCase.vincular(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularServico(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemNotEmDiagnostico() {
        var ordemRecebida = OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, LocalDateTime.now(), null, null, List.of());
        stubOrdem(ordemRecebida);
        stubServico();

        assertThrows(VinculoServicoNaoAutorizadoException.class,
                () -> vincularServicoOrdemDeServicoUseCase.vincular(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).vincularServico(Mockito.any(), Mockito.any());
    }
}
