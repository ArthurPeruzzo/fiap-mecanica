package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.Orcamento;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.ServicoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.IniciarServicoNaoAutorizadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoJaIniciadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.IniciarServicoOrdemDeServicoUseCase;
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
class IniciarServicoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private IniciarServicoOrdemDeServicoUseCase iniciarServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private ServicoGateway servicoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long SERVICO_ID = 10L;
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemEmExecucao(StatusServico statusServico) {
        return OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_EXECUCAO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ServicoVinculado(SERVICO_ID, BigDecimal.TEN, statusServico, null, null)),
                List.of(), List.of(), new Orcamento(BigDecimal.TEN), LocalDateTime.now(), null, LocalDateTime.now());
    }

    private void stubOrdem(OrdemDeServico os) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(os));
    }

    private void stubServico() {
        Mockito.when(servicoGateway.buscarPorId(SERVICO_ID))
                .thenReturn(Optional.of(Servico.reconstituir(SERVICO_ID, "Troca de óleo", "desc", BigDecimal.TEN)));
    }

    @Test
    void shouldIniciarServicoSuccessfully() {
        stubOrdem(ordemEmExecucao(StatusServico.NAO_INICIADO));
        stubServico();

        var captor = ArgumentCaptor.forClass(LocalDateTime.class);

        iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID);

        Mockito.verify(ordemDeServicoGateway).atualizarServico(
                Mockito.eq(ORDEM_ID),
                Mockito.eq(SERVICO_ID),
                Mockito.eq(StatusServico.EM_EXECUCAO),
                captor.capture(),
                Mockito.isNull()
        );
        assertNotNull(captor.getValue());
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verifyNoInteractions(servicoGateway);
        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNotFound() {
        stubOrdem(ordemEmExecucao(StatusServico.NAO_INICIADO));
        Mockito.when(servicoGateway.buscarPorId(SERVICO_ID)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenOrdemNaoEmExecucao() {
        var ordemRecebida = OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null,
                List.of(), List.of(), List.of(), null, null, null, null);
        stubOrdem(ordemRecebida);
        stubServico();

        assertThrows(IniciarServicoNaoAutorizadoException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoNaoVinculado() {
        var ordemSemServico = OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_EXECUCAO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(), List.of(), List.of(), new Orcamento(BigDecimal.TEN), LocalDateTime.now(), null, LocalDateTime.now());
        stubOrdem(ordemSemServico);
        stubServico();

        assertThrows(ServicoNaoVinculadoException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowWhenServicoJaIniciado() {
        stubOrdem(ordemEmExecucao(StatusServico.EM_EXECUCAO));
        stubServico();

        assertThrows(ServicoJaIniciadoException.class,
                () -> iniciarServicoUseCase.iniciar(ORDEM_ID, SERVICO_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never())
                .atualizarServico(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}
