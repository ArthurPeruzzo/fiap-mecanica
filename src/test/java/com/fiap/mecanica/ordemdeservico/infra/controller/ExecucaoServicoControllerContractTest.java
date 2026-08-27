package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
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
@WebMvcTest(controllers = ExecucaoServicoHttpController.class)
class ExecucaoServicoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private OrdemDeServicoGateway ordemDeServicoGateway;
    @MockitoBean private ServicoGateway servicoGateway;
    @MockitoBean private NotificacaoGateway notificacaoGateway;
    @MockitoBean private MetricasGateway metricasGateway;

    private static final Long MECANICO_ID = 5L;

    // ---- domain helpers ----

    private Servico servico() {
        return Servico.reconstituir(10L, "Serviço", "desc", BigDecimal.TEN);
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
                .orcamento(new Orcamento(BigDecimal.TEN))
                .state(OrdemDeServicoStateFactory.from(status))
                .build();
    }

    private OrdemDeServico ordemRecebida() {
        return buildOrdem(null, StatusOrdemDeServico.RECEBIDA, List.of());
    }

    private OrdemDeServico ordemEmExecucaoSemServicos() {
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_EXECUCAO, List.of());
    }

    private OrdemDeServico ordemEmExecucaoComServicoNaoIniciado() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_EXECUCAO, List.of(sv));
    }

    private OrdemDeServico ordemEmExecucaoComServicoEmExecucao() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.EM_EXECUCAO, LocalDateTime.now(), null);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_EXECUCAO, List.of(sv));
    }

    // ---- iniciarServico ----

    @Test
    void shouldReturn204WhenIniciarServicoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoComServicoNaoIniciado()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/iniciar"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnIniciarServico() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/servicos/10/iniciar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnIniciarServico() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/99/iniciar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoNaoVinculadoOnIniciarServico() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/iniciar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmExecucaoOnIniciarServico() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/iniciar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível iniciar um serviço se a ordem de serviço não está em execução"));
    }

    @Test
    void shouldReturn422WhenServicoJaIniciadoOnIniciarServico() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoComServicoEmExecucao()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/iniciar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço já foi iniciado ou finalizado"));
    }

    // ---- finalizarServico ----

    @Test
    void shouldReturn204WhenFinalizarServicoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoComServicoEmExecucao()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/finalizar"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnFinalizar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/servicos/10/finalizar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnFinalizar() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/99/finalizar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoNaoVinculadoOnFinalizar() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/finalizar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmExecucaoOnFinalizar() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/finalizar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível finalizar um serviço se a ordem de serviço não está em execução"));
    }

    @Test
    void shouldReturn422WhenServicoNaoIniciadoOuFinalizadoOnFinalizar() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoComServicoNaoIniciado()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/servicos/10/finalizar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço ainda não foi iniciado ou já foi finalizado"));
    }
}
