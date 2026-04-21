package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoJaExisteException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.usecase.AtualizarVeiculoUseCase;
import com.fiap.mecanica.gestao.core.usecase.CriarVeiculoUseCase;
import com.fiap.mecanica.gestao.core.usecase.ListarVeiculosUseCase;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.page.Pagina;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.List;
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
@WebMvcTest(controllers = VeiculoController.class)
class VeiculoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarVeiculoUseCase criarVeiculoUseCase;

    @MockitoBean
    private AtualizarVeiculoUseCase atualizarVeiculoUseCase;

    @MockitoBean
    private ListarVeiculosUseCase listarVeiculosUseCase;

    // -------------------------------------------------------------------------
    // Validações de campos obrigatórios
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "'{\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}', clienteId, 'O id do cliente deve ser preenchido'",
            "'{\"clienteId\":1,\"placa\":\"ABC1234\",\"modelo\":\"\",\"ano\":2020}', modelo, 'O modelo deve ser preenchido'",
            "'{\"clienteId\":1,\"placa\":\"ABC1234\",\"modelo\":\"Gol\"}', ano, 'O ano deve ser preenchido'"
    })
    void shouldReturn400WithFieldValidationMessage(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(criarVeiculoUseCase);
    }

    // -------------------------------------------------------------------------
    // Validação de placa: @PlacaValida
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "ABCD123",
            "AB12345",
            "ABC123",
            "12345678",
            "ABC12D3"
    })
    void shouldReturn400WhenPlacaIsInvalid(String placa) throws Exception {
        String json = String.format(
                "{\"clienteId\":1,\"placa\":\"%s\",\"modelo\":\"Gol\",\"ano\":2020}", placa);

        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.placa").value(
                        "A placa informada é inválida. Formatos aceitos: ABC1234 (antiga) ou ABC1D23 (Mercosul), com ou sem hífen"));

        Mockito.verifyNoInteractions(criarVeiculoUseCase);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void shouldReturn201WhenValidPlacaAntiga() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isCreated());

        Mockito.verify(criarVeiculoUseCase).criar(Mockito.any());
    }

    @Test
    void shouldReturn201WhenValidPlacaMercosul() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"placa\":\"ABC1D23\",\"modelo\":\"Onix\",\"ano\":2023}"))
                .andExpect(status().isCreated());

        Mockito.verify(criarVeiculoUseCase).criar(Mockito.any());
    }

    @Test
    void shouldReturn201WhenPlacaWithHyphen() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"placa\":\"ABC-1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isCreated());

        Mockito.verify(criarVeiculoUseCase).criar(Mockito.any());
    }

    // -------------------------------------------------------------------------
    // Erros de negócio
    // -------------------------------------------------------------------------

    @Test
    void shouldReturn404WhenClienteNotFound() throws Exception {
        Mockito.doThrow(new ClienteNaoEncontradoException())
                .when(criarVeiculoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":99,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }

    @Test
    void shouldReturn409WhenPlacaAlreadyExists() throws Exception {
        Mockito.doThrow(new VeiculoJaExisteException())
                .when(criarVeiculoUseCase).criar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um veículo cadastrado com a placa informada"));
    }

    // -------------------------------------------------------------------------
    // PUT /veiculo/{id}
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "'{\"placa\":\"ABC1234\",\"modelo\":\"\",\"ano\":2020}', modelo, 'O modelo deve ser preenchido'",
            "'{\"placa\":\"ABC1234\",\"modelo\":\"Gol\"}', ano, 'O ano deve ser preenchido'"
    })
    void shouldReturn400WithFieldValidationMessageOnUpdate(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/veiculo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(atualizarVeiculoUseCase);
    }

    @Test
    void shouldReturn400WhenPlacaIsInvalidOnUpdate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/veiculo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"INVALIDA\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.placa").value(
                        "A placa informada é inválida. Formatos aceitos: ABC1234 (antiga) ou ABC1D23 (Mercosul), com ou sem hífen"));

        Mockito.verifyNoInteractions(atualizarVeiculoUseCase);
    }

    @Test
    void shouldReturn204WhenValidUpdateRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/veiculo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC1D23\",\"modelo\":\"Onix\",\"ano\":2023}"))
                .andExpect(status().isNoContent());

        Mockito.verify(atualizarVeiculoUseCase).atualizar(Mockito.any());
    }

    @Test
    void shouldReturn404WhenVeiculoNotFoundOnUpdate() throws Exception {
        Mockito.doThrow(new VeiculoNaoEncontradoException())
                .when(atualizarVeiculoUseCase).atualizar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.put("/veiculo/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Veículo não encontrado"));
    }

    @Test
    void shouldReturn409WhenPlacaAlreadyExistsOnUpdate() throws Exception {
        Mockito.doThrow(new VeiculoJaExisteException())
                .when(atualizarVeiculoUseCase).atualizar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.put("/veiculo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um veículo cadastrado com a placa informada"));
    }

    // -------------------------------------------------------------------------
    // GET /veiculo
    // -------------------------------------------------------------------------

    @Test
    void shouldReturn200WithEmptyPageWhenNoVeiculos() throws Exception {
        Mockito.when(listarVeiculosUseCase.listar(Mockito.any()))
                .thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(MockMvcRequestBuilders.get("/veiculo")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));

        Mockito.verify(listarVeiculosUseCase).listar(Mockito.any());
    }

    @Test
    void shouldReturn200WithVeiculosMappedToResponseJson() throws Exception {
        var veiculo = Veiculo.reconstituir(1L, 2L, "ABC1234", "Gol", 2020);
        Mockito.when(listarVeiculosUseCase.listar(Mockito.any()))
                .thenReturn(new Pagina<>(List.of(veiculo), 0, 10, 1L, 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/veiculo")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].clienteId").value(2))
                .andExpect(jsonPath("$.content[0].placa").value("ABC-1234"))
                .andExpect(jsonPath("$.content[0].modelo").value("Gol"))
                .andExpect(jsonPath("$.content[0].ano").value(2020))
                .andExpect(jsonPath("$.totalElements").value(1));

        Mockito.verify(listarVeiculosUseCase).listar(Mockito.any());
    }

    @Test
    void shouldReturn200WithDefaultPaginationWhenParamsOmitted() throws Exception {
        Mockito.when(listarVeiculosUseCase.listar(Mockito.any()))
                .thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(MockMvcRequestBuilders.get("/veiculo"))
                .andExpect(status().isOk());

        Mockito.verify(listarVeiculosUseCase).listar(Mockito.any());
    }
}
