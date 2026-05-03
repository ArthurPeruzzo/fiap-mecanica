package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.Orcamento;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.ServicoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.FinalizarServicoNaoAutorizadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoIniciadoOuFinalizadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.FinalizarServicoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
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
class FinalizarServicoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private FinalizarServicoOrdemDeServicoUseCase finalizarServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private ServicoGateway servicoGateway;

    @Mock
    private NotificacaoGateway notificacaoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 2L;
    private static final Long SERVICO_ID = 10L;
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmExecucaoComServico(Long servicoId, StatusServico statusServico) {
        return OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 3L, 4L, 5L,
                StatusOrdemDeServico.EM_EXECUCAO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ServicoVinculado(servicoId, BigDecimal.TEN, statusServico, LocalDateTime.now().minusHours(1), null)),
                List.of(), List.of(), new Orcamento(BigDecimal.TEN), LocalDateTime.now(), null, LocalDateTime.now(), null, null);
    }

    private OrdemDeServico ordemEmExecucaoComDoisServicos(StatusServico statusServico1, StatusServico statusServico2) {
        return OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 3L, 4L, 5L,
                StatusOrdemDeServico.EM_EXECUCAO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(
                        new ServicoVinculado(SERVICO_ID, BigDecimal.TEN, statusServico1, LocalDateTime.now().minusHours(1), null),
                        new ServicoVinculado(20L, BigDecimal.TEN, statusServico2, LocalDateTime.now().minusHours(1), null)
                ),
                List.of(), List.of(), new Orcamento(BigDecimal.TEN), LocalDateTime.now(), null, LocalDateTime.now(), null, null);
    }

    private void stubOrdem(OrdemDeServico os) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(os));
    }

    private void stubServico() {
        Mockito.when(servicoGateway.buscarPorId(SERVICO_ID))
                .thenReturn(Optional.of(Servico.reconstituir(SERVICO_ID, "Troca de óleo", "desc", BigDecimal.TEN)));
    }

    @Test
    void shouldFinalizarServicoSuccessfully() {
        stubOrdem(ordemEmExecucaoComDoisServicos(StatusServico.EM_EXECUCAO, StatusServico.NAO_INICIADO));
        stubServico();

        var dataFimCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        finalizarServicoUseCase.finalizar(ORDEM_ID, SERVICO_ID);

        Mockito.verify(ordemDeServicoGateway).atualizarServico(
                Mockito.eq(ORDEM_ID),
                Mockito.eq(SERVICO_ID),
                Mockito.eq(StatusServico.FINALIZADO),
                Mockito.any(LocalDateTime.class),
                dataFimCaptor.capture()
        );
        assertNotNull(dataFimCaptor.getValue());
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldFinalizarOSENotificarQuandoUltimoServico() {
        stubOrdem(ordemEmExecucaoComServico(SERVICO_ID, StatusServico.EM_EXECUCAO));
        stubServico();

        finalizarServicoUseCase.finalizar(ORDEM_ID, SERVICO_ID);

        Mockito.verify(ordemDeServicoGateway).atualizarServico(
                Mockito.eq(ORDEM_ID),
                Mockito.eq(SERVICO_ID),
                Mockito.eq(StatusServico.FINALIZADO),
                Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class)
        );
        Mockito.verify(ordemDeServicoGateway).atualizar(Mockito.argThat(
                os -> StatusOrdemDeServico.FINALIZADA.equals(os.getStatus())));
        Mockito.verify(notificacaoGateway).notificarServicoFinalizado(CLIENTE_ID);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> finalizarServicoUseCase.finalizar(ORDEM_ID, SERVICO_ID));

        Mockito.verifyNoInteractions(servicoGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNotFound() {
        stubOrdem(ordemEmExecucaoComServico(SERVICO_ID, StatusServico.EM_EXECUCAO));
        Mockito.when(servicoGateway.buscarPorId(SERVICO_ID)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class,
                () -> finalizarServicoUseCase.finalizar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemNaoEmExecucao() {
        var ordemRecebida = OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 3L, 4L, null,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null,
                List.of(), List.of(), List.of(), null, null, null, null, null, null);
        stubOrdem(ordemRecebida);
        stubServico();

        assertThrows(FinalizarServicoNaoAutorizadoException.class,
                () -> finalizarServicoUseCase.finalizar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNaoVinculado() {
        var ordemSemServico = OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 3L, 4L, 5L,
                StatusOrdemDeServico.EM_EXECUCAO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(), List.of(), List.of(), new Orcamento(BigDecimal.TEN), LocalDateTime.now(), null, LocalDateTime.now(), null, null);
        stubOrdem(ordemSemServico);
        stubServico();

        assertThrows(ServicoNaoVinculadoException.class,
                () -> finalizarServicoUseCase.finalizar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNaoIniciado() {
        stubOrdem(ordemEmExecucaoComServico(SERVICO_ID, StatusServico.NAO_INICIADO));
        stubServico();

        assertThrows(ServicoNaoIniciadoOuFinalizadoException.class,
                () -> finalizarServicoUseCase.finalizar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoJaFinalizado() {
        stubOrdem(ordemEmExecucaoComServico(SERVICO_ID, StatusServico.FINALIZADO));
        stubServico();

        assertThrows(ServicoNaoIniciadoOuFinalizadoException.class,
                () -> finalizarServicoUseCase.finalizar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}
