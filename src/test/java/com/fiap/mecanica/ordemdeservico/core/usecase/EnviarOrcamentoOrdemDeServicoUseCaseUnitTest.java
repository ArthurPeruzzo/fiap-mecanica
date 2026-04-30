package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.Orcamento;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.ServicoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.EnviarOrcamentoOrdemDeServicoUseCase;
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
class EnviarOrcamentoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private EnviarOrcamentoOrdemDeServicoUseCase enviarOrcamentoOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private NotificacaoGateway notificacaoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 10L;
    private static final BigDecimal VALOR_ORCAMENTO = new BigDecimal("350.00");
    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemDiagnosticoConcluido() {
        return OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 2L, 3L, 5L,
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, DESCRICAO,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ServicoVinculado(10L, VALOR_ORCAMENTO)),
                List.of(), List.of(),
                new Orcamento(VALOR_ORCAMENTO), null);
    }

    @Test
    void shouldEnviarOrcamentoSuccessfully() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemDiagnosticoConcluido()));

        enviarOrcamentoOrdemDeServicoUseCase.enviar(ORDEM_ID);

        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(captor.capture());
        var os = captor.getValue();
        assertEquals(StatusOrdemDeServico.AGUARDANDO_APROVACAO, os.getStatus());
        assertNotNull(os.getDataEnvioOrcamento());
        Mockito.verify(notificacaoGateway).enviarOrcamento(CLIENTE_ID, VALOR_ORCAMENTO);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> enviarOrcamentoOrdemDeServicoUseCase.enviar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldThrowWhenStatusInvalido() {
        var ordemEmDiagnostico = OrdemDeServico.reconstituir(ORDEM_ID, CLIENTE_ID, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO,
                LocalDateTime.now(), LocalDateTime.now(), null,
                List.of(), List.of(), List.of(), null, null);
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemEmDiagnostico));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> enviarOrcamentoOrdemDeServicoUseCase.enviar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldNotSendNotificacaoWhenAtualizarFails() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemDiagnosticoConcluido()));
        Mockito.doThrow(new RuntimeException("db error")).when(ordemDeServicoGateway).atualizar(Mockito.any());

        assertThrows(RuntimeException.class,
                () -> enviarOrcamentoOrdemDeServicoUseCase.enviar(ORDEM_ID));

        Mockito.verifyNoInteractions(notificacaoGateway);
    }
}
