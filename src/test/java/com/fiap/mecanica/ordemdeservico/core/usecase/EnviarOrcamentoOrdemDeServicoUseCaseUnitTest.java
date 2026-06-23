package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.EnviarOrcamentoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;

    @Mock
    private NotificacaoGateway notificacaoGateway;

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 10L;
    private static final BigDecimal VALOR_ORCAMENTO = new BigDecimal("350.00");
    private static final String DESCRICAO = "Barulho ao frear";
    private static final String URL_APROVAR = "http://localhost:8080/ordem-servico/orcamento/externo/aprovar/";
    private static final String URL_RECUSAR = "http://localhost:8080/ordem-servico/orcamento/externo/recusar/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(enviarOrcamentoOrdemDeServicoUseCase, "urlAprovarOrcamento", URL_APROVAR);
        ReflectionTestUtils.setField(enviarOrcamentoOrdemDeServicoUseCase, "urlRecusarOrcamento", URL_RECUSAR);
    }

    private OrdemDeServico ordemDiagnosticoConcluido() {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(CLIENTE_ID)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(10L, VALOR_ORCAMENTO, StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(VALOR_ORCAMENTO))
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();
    }

    @Test
    void shouldEnviarOrcamentoSuccessfully() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemDiagnosticoConcluido()));

        enviarOrcamentoOrdemDeServicoUseCase.enviar(ORDEM_ID);

        var osCaptor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(osCaptor.capture());
        var os = osCaptor.getValue();
        assertEquals(StatusOrdemDeServico.AGUARDANDO_APROVACAO, os.getStatus());
        assertNotNull(os.getDataEnvioOrcamento());

        var linkCaptor = ArgumentCaptor.forClass(LinkAprovacaoOrcamento.class);
        Mockito.verify(linkAprovacaoOrcamentoGateway).salvar(linkCaptor.capture());
        var link = linkCaptor.getValue();
        assertEquals(ORDEM_ID, link.getOrdemDeServicoId());
        assertNotNull(link.getToken());

        var mensagemCaptor = ArgumentCaptor.forClass(Mensagem.class);
        Mockito.verify(notificacaoGateway).enviar(mensagemCaptor.capture());
        var mensagem = mensagemCaptor.getValue();
        assertEquals(CLIENTE_ID, mensagem.getClienteId());
        assertTrue(mensagem.getConteudo().contains(VALOR_ORCAMENTO.toString()));
        assertTrue(mensagem.getConteudo().contains(link.getToken()));
        assertTrue(mensagem.getConteudo().contains(URL_APROVAR));
        assertTrue(mensagem.getConteudo().contains(URL_RECUSAR));
    }

    @Test
    void shouldThrowWhenOrdemDeServicoNotFound() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> enviarOrcamentoOrdemDeServicoUseCase.enviar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(linkAprovacaoOrcamentoGateway);
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldThrowWhenStatusInvalido() {
        var ordemEmDiagnostico = OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(CLIENTE_ID)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.EM_DIAGNOSTICO))
                .build();
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemEmDiagnostico));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> enviarOrcamentoOrdemDeServicoUseCase.enviar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verifyNoInteractions(linkAprovacaoOrcamentoGateway);
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldNotSalvarLinkNemEnviarNotificacaoWhenAtualizarFails() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemDiagnosticoConcluido()));
        Mockito.doThrow(new RuntimeException("db error")).when(ordemDeServicoGateway).atualizar(Mockito.any());

        assertThrows(RuntimeException.class,
                () -> enviarOrcamentoOrdemDeServicoUseCase.enviar(ORDEM_ID));

        Mockito.verifyNoInteractions(linkAprovacaoOrcamentoGateway);
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldNotEnviarNotificacaoWhenSalvarLinkFails() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordemDiagnosticoConcluido()));
        Mockito.doThrow(new RuntimeException("db error")).when(linkAprovacaoOrcamentoGateway).salvar(Mockito.any());

        assertThrows(RuntimeException.class,
                () -> enviarOrcamentoOrdemDeServicoUseCase.enviar(ORDEM_ID));

        Mockito.verifyNoInteractions(notificacaoGateway);
    }
}
