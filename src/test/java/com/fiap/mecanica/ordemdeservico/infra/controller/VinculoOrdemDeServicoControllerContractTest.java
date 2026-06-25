package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
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
@WebMvcTest(controllers = VinculoOrdemDeServicoHttpController.class)
class VinculoOrdemDeServicoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private OrdemDeServicoGateway ordemDeServicoGateway;
    @MockitoBean private ServicoGateway servicoGateway;
    @MockitoBean private PecaGateway pecaGateway;
    @MockitoBean private InsumoGateway insumoGateway;

    private static final Long MECANICO_ID = 5L;

    private static final String VALID_PECA_BODY = "{\"quantidade\":2}";
    private static final String VALID_INSUMO_BODY = "{\"quantidade\":3}";

    // ---- domain helpers ----

    private OrdemDeServico buildOrdem(Long mecanicoId, StatusOrdemDeServico status,
                                      List<ServicoVinculado> servicos,
                                      List<PecaVinculada> pecas,
                                      List<InsumoVinculado> insumos) {
        return OrdemDeServico.builder()
                .id(1L).clienteId(1L).veiculoId(2L).atendenteId(3L).mecanicoId(mecanicoId)
                .status(status).descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(servicos))
                .pecasVinculadas(new ArrayList<>(pecas))
                .insumosVinculados(new ArrayList<>(insumos))
                .state(OrdemDeServicoStateFactory.from(status))
                .build();
    }

    private OrdemDeServico ordemRecebida() {
        return buildOrdem(null, StatusOrdemDeServico.RECEBIDA, List.of(), List.of(), List.of());
    }

    private OrdemDeServico ordemRecebidaComServico() {
        var sv = new ServicoVinculado(10L, BigDecimal.TEN, StatusServico.NAO_INICIADO, null, null);
        return buildOrdem(null, StatusOrdemDeServico.RECEBIDA, List.of(sv), List.of(), List.of());
    }

    private OrdemDeServico ordemEmDiagnostico() {
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(), List.of(), List.of());
    }

    private OrdemDeServico ordemEmDiagnosticoComPeca(Integer quantidade) {
        var pv = new PecaVinculada(5L, quantidade, BigDecimal.TEN);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(), List.of(pv), List.of());
    }

    private OrdemDeServico ordemEmDiagnosticoComInsumo(Integer quantidade) {
        var iv = new InsumoVinculado(30L, quantidade, BigDecimal.TEN);
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_DIAGNOSTICO, List.of(), List.of(), List.of(iv));
    }

    private OrdemDeServico ordemEmExecucaoSemServicos() {
        return buildOrdem(MECANICO_ID, StatusOrdemDeServico.EM_EXECUCAO, List.of(), List.of(), List.of());
    }

    private Servico servico() {
        return Servico.reconstituir(10L, "Serviço", "desc", BigDecimal.TEN);
    }

    private Peca pecaComEstoque(int estoque) {
        return Peca.reconstituir(5L, "Peca", "desc", BigDecimal.TEN, estoque);
    }

    private Insumo insumoComEstoque(int estoque) {
        return Insumo.reconstituir(30L, "Insumo", "desc", BigDecimal.TEN, UnidadeMedida.LITRO, estoque);
    }

    // ---- vincularServico ----

    @Test
    void shouldReturn204WhenVincularServicoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincular() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/99/servicos/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnVincular() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoJaVinculado() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebidaComServico()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço já está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoERecebidaOnVincular() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    // ---- desvincularServico ----

    @Test
    void shouldReturn204WhenDesvincularServicoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebidaComServico()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincular() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/99/servicos/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnDesvincular() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @Test
    void shouldReturn422WhenServicoNaoVinculado() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Este serviço não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnDesvincular() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));
        Mockito.when(servicoGateway.buscarPorId(10L)).thenReturn(Optional.of(servico()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/servicos/10"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    // ---- vincularPeca ----

    @Test
    void shouldReturn204WhenVincularPecaSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(pecaComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @CsvSource({
            "'{}'                 , quantidade, 'A quantidade deve ser informada'",
            "'{\"quantidade\":0}' , quantidade, 'A quantidade deve ser no mínimo 1'",
            "'{\"quantidade\":-1}', quantidade, 'A quantidade deve ser no mínimo 1'"
    })
    void shouldReturn400WhenVincularPecaRequestIsInvalid(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularPeca() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/99/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnVincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(pecaComEstoque(0)));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnVincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(pecaComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível vincular peças se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    // ---- desvincularPeca ----

    @Test
    void shouldReturn204WhenDesvincularPecaSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComPeca(5)));
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(pecaComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @CsvSource({
            "'{}'                 , quantidade, 'A quantidade deve ser informada'",
            "'{\"quantidade\":0}' , quantidade, 'A quantidade deve ser no mínimo 1'",
            "'{\"quantidade\":-1}', quantidade, 'A quantidade deve ser no mínimo 1'"
    })
    void shouldReturn400WhenDesvincularPecaRequestIsInvalid(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularPeca() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/99/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnDesvincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnDesvincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(pecaComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível desvincular peças se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenPecaNaoVinculadaOnDesvincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnostico()));
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(pecaComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Peça não está vinculada à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeDesvincularInvalidaOnDesvincularPeca() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComPeca(1)));
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(pecaComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/pecas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PECA_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Quantidade a desvincular é maior que a quantidade vinculada"));
    }

    // ---- vincularInsumo ----

    @Test
    void shouldReturn204WhenVincularInsumoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumoComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @CsvSource({
            "'{}'                 , quantidade, 'A quantidade deve ser informada'",
            "'{\"quantidade\":0}' , quantidade, 'A quantidade deve ser no mínimo 1'",
            "'{\"quantidade\":-1}', quantidade, 'A quantidade deve ser no mínimo 1'"
    })
    void shouldReturn400WhenVincularInsumoRequestIsInvalid(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularInsumo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/99/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnVincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumoComEstoque(0)));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnVincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmExecucaoSemServicos()));
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumoComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.put("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível vincular insumos se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    // ---- desvincularInsumo ----

    @Test
    void shouldReturn204WhenDesvincularInsumoSuccess() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComInsumo(5)));
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumoComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @CsvSource({
            "'{}'                 , quantidade, 'A quantidade deve ser informada'",
            "'{\"quantidade\":0}' , quantidade, 'A quantidade deve ser no mínimo 1'",
            "'{\"quantidade\":-1}', quantidade, 'A quantidade deve ser no mínimo 1'"
    })
    void shouldReturn400WhenDesvincularInsumoRequestIsInvalid(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularInsumo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/99/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnDesvincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Insumo não encontrado"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoOnDesvincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemRecebida()));
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumoComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível desvincular insumos se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenInsumoNaoVinculadoOnDesvincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnostico()));
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumoComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Insumo não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeDesvincularInvalidaOnDesvincularInsumo() throws Exception {
        Mockito.when(ordemDeServicoGateway.buscarPorId(1L)).thenReturn(Optional.of(ordemEmDiagnosticoComInsumo(1)));
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumoComEstoque(10)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/ordem-servico/1/insumos/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INSUMO_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Quantidade a desvincular é maior que a quantidade vinculada"));
    }
}
