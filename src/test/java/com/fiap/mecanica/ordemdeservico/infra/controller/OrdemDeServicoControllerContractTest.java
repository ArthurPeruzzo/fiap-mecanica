package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.core.exception.AtendenteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoAbertaParaVeiculoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoEmDiagnosticoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoMecanicoResponsavelException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import com.fiap.mecanica.ordemdeservico.core.exception.VeiculoNaoPertenceAoClienteException;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ConcluirDiagnosticoOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.CriarOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.IniciarDiagnosticoOrdemDeServicoUseCase;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = OrdemDeServicoController.class)
class OrdemDeServicoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarOrdemDeServicoUseCase criarOrdemDeServicoUseCase;

    @MockitoBean
    private IniciarDiagnosticoOrdemDeServicoUseCase iniciarDiagnosticoOrdemDeServicoUseCase;

    @MockitoBean
    private ConcluirDiagnosticoOrdemDeServicoUseCase concluirDiagnosticoOrdemDeServicoUseCase;

    private static final String VALID_BODY = "{\"clienteId\":1,\"veiculoId\":2}";

    @Test
    void shouldReturn201WhenValidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        Mockito.verify(criarOrdemDeServicoUseCase).criar(Mockito.any());
    }

    @ParameterizedTest
    @CsvSource({
            "'{\"veiculoId\":2}', clienteId, 'O cliente deve ser informado'",
            "'{\"clienteId\":1}', veiculoId, 'O veículo deve ser informado'"
    })
    void shouldReturn400WhenRequiredFieldIsMissing(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(criarOrdemDeServicoUseCase);
    }

    @Test
    void shouldReturn404WhenAtendenteNotFound() throws Exception {
        Mockito.doThrow(new AtendenteNaoEncontradoException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Atendente não encontrado"));
    }

    @Test
    void shouldReturn404WhenClienteNotFound() throws Exception {
        Mockito.doThrow(new ClienteNaoEncontradoException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }

    @Test
    void shouldReturn404WhenVeiculoNotFound() throws Exception {
        Mockito.doThrow(new VeiculoNaoEncontradoException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Veículo não encontrado"));
    }

    @Test
    void shouldReturn422WhenVeiculoNaoPertenceAoCliente() throws Exception {
        Mockito.doThrow(new VeiculoNaoPertenceAoClienteException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Veículo não pertence ao cliente informado"));
    }

    @Test
    void shouldReturn422WhenOrdemAbertaExistsForVeiculo() throws Exception {
        Mockito.doThrow(new OrdemDeServicoAbertaParaVeiculoException()).when(criarOrdemDeServicoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Já existe uma ordem de serviço aberta para este veículo"));
    }

    // --- iniciarDiagnostico ---

    @Test
    void shouldReturn204WhenIniciarDiagnosticoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isNoContent());

        Mockito.verify(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);
    }

    @Test
    void shouldReturn404WhenOrdemDeServicoNotFoundOnIniciar() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(99L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/diagnostico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenMecanicoNotFoundOnIniciar() throws Exception {
        Mockito.doThrow(new MecanicoNaoEncontradoException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mecânico não encontrado"));
    }

    @Test
    void shouldReturn409WhenOrdemJaEmDiagnostico() throws Exception {
        Mockito.doThrow(new OrdemDeServicoEmDiagnosticoException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A ordem de servico ja esta em diagnostico"));
    }

    @Test
    void shouldReturn422WhenOutroMecanicoJaVinculado() throws Exception {
        Mockito.doThrow(new OrdemDeServicoMecanicoResponsavelException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Já existe um mecanico responsavel pela ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenTransicaoInvalidaOnIniciar() throws Exception {
        Mockito.doThrow(new TransicaoDeStatusInvalidaException())
                .when(iniciarDiagnosticoOrdemDeServicoUseCase).iniciarDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }

    // --- concluirDiagnostico ---

    @Test
    void shouldReturn204WhenConcluirDiagnosticoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isNoContent());

        Mockito.verify(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(1L);
    }

    @Test
    void shouldReturn404WhenOrdemDeServicoNotFoundOnConcluir() throws Exception {
        Mockito.doThrow(new OrdemDeServicoNaoEncontradaException())
                .when(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(99L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/99/diagnostico/conclusao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenMecanicoNotFoundOnConcluir() throws Exception {
        Mockito.doThrow(new MecanicoNaoEncontradoException())
                .when(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mecânico não encontrado"));
    }

    @Test
    void shouldReturn422WhenTransicaoInvalidaOnConcluir() throws Exception {
        Mockito.doThrow(new TransicaoDeStatusInvalidaException())
                .when(concluirDiagnosticoOrdemDeServicoUseCase).concluirDiagnostico(1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/ordem-servico/1/diagnostico/conclusao"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("A ordem de serviço não está no status correto para esta operação"));
    }
}
