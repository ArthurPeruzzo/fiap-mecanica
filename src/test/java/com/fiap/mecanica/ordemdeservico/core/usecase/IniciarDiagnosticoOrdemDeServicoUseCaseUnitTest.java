package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoMecanicoResponsavelException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.IniciarDiagnosticoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class IniciarDiagnosticoOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private IniciarDiagnosticoOrdemDeServicoUseCase iniciarDiagnosticoOrdemDeServicoUseCase;

    @Mock
    private MecanicoGateway mecanicoGateway;

    @Mock
    private TokenGateway tokenGateway;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    private static final Long USER_ID = 10L;
    private static final Long MECANICO_ID = 5L;
    private static final Long OUTRO_MECANICO_ID = 99L;
    private static final Long ORDEM_ID = 1L;

    private void stubMecanico() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID))
                .thenReturn(Optional.of(Mecanico.builder().id(MECANICO_ID).build()));
    }

    private static final String DESCRICAO = "Barulho ao frear";

    private OrdemDeServico ordemRecebidaSemMecanico() {
        return OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, null,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null, List.of(), List.of(), List.of(), null, null);
    }

    private OrdemDeServico ordemEmDiagnostico(Long mecanicoId) {
        return OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, mecanicoId,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null, List.of(), List.of(), List.of(), null, null);
    }

    private OrdemDeServico ordemEmOutroStatus(StatusOrdemDeServico status) {
        return OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, MECANICO_ID,
                status, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(), List.of(), List.of(), null, null);
    }

    // --- happy path ---

    @Test
    void shouldIniciarDiagnosticoWhenOrdemSemMecanico() {
        stubMecanico();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID))
                .thenReturn(Optional.of(ordemRecebidaSemMecanico()));

        iniciarDiagnosticoOrdemDeServicoUseCase.iniciarDiagnostico(ORDEM_ID);

        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(captor.capture());
        var os = captor.getValue();
        assertEquals(MECANICO_ID, os.getMecanicoId());
        assertEquals(StatusOrdemDeServico.EM_DIAGNOSTICO, os.getStatus());
        assertNotNull(os.getDataInicioDiagnostico());
    }

    // --- ordemDeServico já em diagnóstico ---

    @Test
    void shouldThrowWhenStatusIsEmDiagnostico() {
        stubMecanico();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID))
                .thenReturn(Optional.of(ordemEmDiagnostico(MECANICO_ID)));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> iniciarDiagnosticoOrdemDeServicoUseCase.iniciarDiagnostico(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowMecanicoResponsavelWhenOutroMecanicoEStatusIsEmDiagnostico() {
        stubMecanico();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID))
                .thenReturn(Optional.of(ordemEmDiagnostico(OUTRO_MECANICO_ID)));

        // check do mecânico tem precedência — lança MecanicoResponsavel, não EmDiagnostico
        assertThrows(OrdemDeServicoMecanicoResponsavelException.class,
                () -> iniciarDiagnosticoOrdemDeServicoUseCase.iniciarDiagnostico(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    // --- mecânico diferente já vinculado ---

    @Test
    void shouldThrowMecanicoResponsavelWhenOutroMecanicoJaVinculado() {
        stubMecanico();
        // OS ainda RECEBIDA mas com outro mecânico registrado
        var ordemComOutroMecanico = OrdemDeServico.reconstituir(ORDEM_ID, 1L, 2L, 3L, OUTRO_MECANICO_ID,
                StatusOrdemDeServico.RECEBIDA, DESCRICAO, LocalDateTime.now(), null, null, List.of(), List.of(), List.of(), null, null);
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID))
                .thenReturn(Optional.of(ordemComOutroMecanico));

        assertThrows(OrdemDeServicoMecanicoResponsavelException.class,
                () -> iniciarDiagnosticoOrdemDeServicoUseCase.iniciarDiagnostico(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    // --- status inválido para a transição (nem RECEBIDA nem EM_DIAGNOSTICO) ---

    @Test
    void shouldThrowTransicaoInvalidaWhenStatusIsDiagnosticoConcluido() {
        stubMecanico();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID))
                .thenReturn(Optional.of(ordemEmOutroStatus(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> iniciarDiagnosticoOrdemDeServicoUseCase.iniciarDiagnostico(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    // --- infraestrutura ---

    @Test
    void shouldThrowWhenMecanicoNotFound() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(MecanicoNaoEncontradoException.class,
                () -> iniciarDiagnosticoOrdemDeServicoUseCase.iniciarDiagnostico(ORDEM_ID));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        stubMecanico();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> iniciarDiagnosticoOrdemDeServicoUseCase.iniciarDiagnostico(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }
}
