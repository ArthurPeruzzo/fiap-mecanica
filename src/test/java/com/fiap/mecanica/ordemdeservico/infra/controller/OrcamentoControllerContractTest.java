package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = OrcamentoHttpController.class)
class OrcamentoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private OrdemDeServicoGateway ordemDeServicoGateway;
    @MockitoBean private PecaGateway pecaGateway;
    @MockitoBean private InsumoGateway insumoGateway;
    @MockitoBean private NotificacaoGateway notificacaoGateway;
    @MockitoBean private LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;

    // ---- domain helpers ----

    private OrdemDeServico buildOrdem(StatusOrdemDeServico status) {
        return OrdemDeServico.builder()
                .id(1L).clienteId(1L).veiculoId(2L).atendenteId(3L).mecanicoId(5L)
                .status(status).descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(BigDecimal.TEN))
                .state(OrdemDeServicoStateFactory.from(status))
                .build();
    }

    private OrdemDeServico ordemDiagnosticoConcluido() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return OrdemDeServico.builder()
                .id(1L).clienteId(1L).veiculoId(2L).atendenteId(3L).mecanicoId(5L)
                .status(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO).descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(sv)))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(new Orcamento(BigDecimal.TEN))
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO))
                .build();
    }

    private OrdemDeServico ordemAguardandoAprovacao() {
        return buildOrdem(StatusOrdemDeServico.AGUARDANDO_APROVACAO);
    }

    private OrdemDeServico ordemRecebida() {
        return buildOrdem(StatusOrdemDeServico.RECEBIDA);
    }

    private LinkAprovacaoOrcamento validLink() {
        return LinkAprovacaoOrcamento.builder()
                .ordemDeServicoId(1L).token("some-token")
                .dataExpiracao(LocalDateTime.now().plusDays(3))
                .build();
    }

    private LinkAprovacaoOrcamento expiredLink() {
        return LinkAprovacaoOrcamento.builder()
                .ordemDeServicoId(1L).token("token-expirado")
                .dataExpiracao(LocalDateTime.now().minusDays(1))
                .build();
    }

    // ---- enviarOrcamento ----

    @Test
    void shouldReturn204WhenEnviarOrcamentoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemDiagnosticoConcluido()));

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico/orcamento/envio/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnEnviarOrcamento() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico/orcamento/envio/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/ordem-servico/orcamento/envio/1",
            "/ordem-servico/orcamento/recusar/1",
            "/ordem-servico/orcamento/aprovar/1"
    })
    void shouldReturn422WhenTransicaoInvalida(String url) throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.post(url))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }

    // ---- recusarOrcamento (atendente) ----

    @Test
    void shouldReturn204WhenRecusarOrcamentoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemAguardandoAprovacao()));

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico/orcamento/recusar/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnRecusar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico/orcamento/recusar/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }


    // ---- aprovarOrcamento (atendente) ----

    @Test
    void shouldReturn204WhenAprovarOrcamentoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemAguardandoAprovacao()));

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico/orcamento/aprovar/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnAprovar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico/orcamento/aprovar/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    // ---- recusar / aprovar via token ----

    @Test
    void shouldReturn204WhenRecusarExternoSuccessfully() throws Exception {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken("some-token")).thenReturn(Optional.of(validLink()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemAguardandoAprovacao()));

        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/orcamento/externo/recusar/some-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenRecusarExternoTokenNaoEncontrado() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/orcamento/externo/recusar/token-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Link de aprovação de orçamento não encontrado"));
    }

    @Test
    void shouldReturn410WhenRecusarExternoTokenInvalido() throws Exception {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken("token-expirado")).thenReturn(Optional.of(expiredLink()));

        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/orcamento/externo/recusar/token-expirado"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("Link de aprovação de orçamento expirado ou já utilizado"));
    }

    @Test
    void shouldReturn204WhenAprovarExternoSuccessfully() throws Exception {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken("some-token")).thenReturn(Optional.of(validLink()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemAguardandoAprovacao()));

        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/orcamento/externo/aprovar/some-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenAprovarExternoTokenNaoEncontrado() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/orcamento/externo/aprovar/token-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Link de aprovação de orçamento não encontrado"));
    }

    @Test
    void shouldReturn410WhenAprovarExternoTokenInvalido() throws Exception {
        Mockito.when(linkAprovacaoOrcamentoGateway.buscarPorToken("token-expirado")).thenReturn(Optional.of(expiredLink()));

        mockMvc.perform(MockMvcRequestBuilders.get("/ordem-servico/orcamento/externo/aprovar/token-expirado"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("Link de aprovação de orçamento expirado ou já utilizado"));
    }
}
