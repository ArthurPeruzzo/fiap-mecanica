package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.Test;
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
@WebMvcTest(controllers = DiagnosticoOrdemDeServicoHttpController.class)
class DiagnosticoOrdemDeServicoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private OrdemDeServicoGateway ordemDeServicoGateway;
    @MockitoBean private MecanicoGateway mecanicoGateway;
    @MockitoBean private TokenGateway tokenGateway;
    @MockitoBean private MetricasGateway metricasGateway;

    private static final Long USER_ID = 10L;
    private static final Long MECANICO_ID = 5L;

    // ---- domain helpers ----

    private Mecanico mecanico() {
        return Mecanico.builder().id(MECANICO_ID).nomeCompleto(new NomeCompleto("Carlos", "Lima")).build();
    }

    private OrdemDeServico buildOrdem(Long mecanicoId, StatusOrdemDeServico status,
                                      List<ServicoVinculado> servicos) {
        return OrdemDeServico.builder()
                .id(1L).clienteId(1L).veiculoId(2L)
                .atendenteId(3L).mecanicoId(mecanicoId)
                .status(status).descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(servicos))
                .pecasVinculadas(new ArrayList<>())
                .insumosVinculados(new ArrayList<>())
                .state(OrdemDeServicoStateFactory.from(status))
                .build();
    }

    private OrdemDeServico ordemRecebida() {
        return buildOrdem(null, StatusOrdemDeServico.RECEBIDA, List.of());
    }

    private OrdemDeServico ordemComMecanicoJaVinculado() {
        return buildOrdem(99L, StatusOrdemDeServico.RECEBIDA, List.of());
    }

    private OrdemDeServico ordemEmDiagnostico() {
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of());
    }

    private OrdemDeServico ordemEmDiagnosticoComServico() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(sv));
    }

    private OrdemDeServico ordemEmDiagnosticoComServicoOutroMecanico() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(99L, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(sv));
    }

    private OrdemDeServico ordemDiagnosticoConcluido() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, List.of(sv));
    }

    // ---- iniciarDiagnostico ----

    @Test
    void shouldReturn204WhenIniciarDiagnosticoSuccess() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemDeServicoNotFoundOnIniciar() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/diagnostico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenMecanicoNotFoundOnIniciar() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mecânico não encontrado"));
    }

    @Test
    void shouldReturn422WhenOutroMecanicoJaVinculado() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemComMecanicoJaVinculado()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Já existe um mecanico responsavel pela ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenTransicaoInvalidaOnIniciar() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComServico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }

    // ---- concluirDiagnostico ----

    @Test
    void shouldReturn204WhenConcluirDiagnosticoSuccess() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComServico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemDeServicoNotFoundOnConcluir() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/diagnostico/conclusao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenMecanicoNotFoundOnConcluir() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mecânico não encontrado"));
    }

    @Test
    void shouldReturn422WhenMecanicoNaoEhResponsavelOnConcluir() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComServicoOutroMecanico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Somente o mecânico responsável pelo diagnóstico pode concluí-lo"));
    }

    @Test
    void shouldReturn422WhenSemServicosVinculadosOnConcluir() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnostico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível concluir o diagnóstico sem ao menos um serviço vinculado"));
    }

    @Test
    void shouldReturn422WhenTransicaoInvalidaOnConcluir() throws Exception {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(mecanicoGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(mecanico()));
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemDiagnosticoConcluido()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }
}
