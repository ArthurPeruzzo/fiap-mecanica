package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.page.Pagina;
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

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = VeiculoHttpController.class)
class VeiculoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VeiculoGateway veiculoGateway;

    @MockitoBean
    private ClienteGateway clienteGateway;

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

        Mockito.verifyNoInteractions(veiculoGateway, clienteGateway);
    }

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

        Mockito.verifyNoInteractions(veiculoGateway, clienteGateway);
    }

    @ParameterizedTest
    @CsvSource({
            "ABC1234,  Gol,  2020",
            "ABC1D23,  Onix, 2023",
            "ABC-1234, Gol,  2020"
    })
    void shouldReturn201WhenValidPlaca(String placa, String modelo, int ano) throws Exception {
        var cliente = Cliente.reconstituir(1L, "Pedro", null, "12345678909");
        Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        Mockito.when(veiculoGateway.existePorPlaca(Mockito.any())).thenReturn(false);

        String json = String.format(
                "{\"clienteId\":1,\"placa\":\"%s\",\"modelo\":\"%s\",\"ano\":%d}", placa, modelo, ano);

        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        Mockito.verify(veiculoGateway, Mockito.atLeastOnce()).criar(Mockito.any());
    }

    @Test
    void shouldReturn404WhenClienteNotFound() throws Exception {
        Mockito.when(clienteGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":99,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }

    @Test
    void shouldReturn409WhenPlacaAlreadyExists() throws Exception {
        var cliente = Cliente.reconstituir(1L, "Pedro", null, "12345678909");
        Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        Mockito.when(veiculoGateway.existePorPlaca("ABC1234")).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/veiculo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um veículo cadastrado com a placa informada"));
    }

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

        Mockito.verifyNoInteractions(veiculoGateway);
    }

    @Test
    void shouldReturn400WhenPlacaIsInvalidOnUpdate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/veiculo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"INVALIDA\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.placa").value(
                        "A placa informada é inválida. Formatos aceitos: ABC1234 (antiga) ou ABC1D23 (Mercosul), com ou sem hífen"));

        Mockito.verifyNoInteractions(veiculoGateway);
    }

    @Test
    void shouldReturn204WhenValidUpdateRequest() throws Exception {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        Mockito.when(veiculoGateway.buscarPorId(1L)).thenReturn(Optional.of(veiculo));
        Mockito.when(veiculoGateway.existePorPlacaExcluindoId("ABC1D23", 1L)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.put("/veiculo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC1D23\",\"modelo\":\"Onix\",\"ano\":2023}"))
                .andExpect(status().isNoContent());

        Mockito.verify(veiculoGateway).atualizar(Mockito.any());
    }

    @Test
    void shouldReturn404WhenVeiculoNotFoundOnUpdate() throws Exception {
        Mockito.when(veiculoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/veiculo/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Veículo não encontrado"));
    }

    @Test
    void shouldReturn409WhenPlacaAlreadyExistsOnUpdate() throws Exception {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        Mockito.when(veiculoGateway.buscarPorId(1L)).thenReturn(Optional.of(veiculo));
        Mockito.when(veiculoGateway.existePorPlacaExcluindoId("ABC1234", 1L)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.put("/veiculo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um veículo cadastrado com a placa informada"));
    }

    @Test
    void shouldReturn204WhenDeletarVeiculoSuccessfully() throws Exception {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        Mockito.when(veiculoGateway.buscarPorId(1L)).thenReturn(Optional.of(veiculo));

        mockMvc.perform(MockMvcRequestBuilders.delete("/veiculo/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(veiculoGateway).deletar(1L);
    }

    @Test
    void shouldReturn404WhenVeiculoNotFoundOnDelete() throws Exception {
        Mockito.when(veiculoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/veiculo/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Veículo não encontrado"));
    }

    @Test
    void shouldReturn200WithEmptyPageWhenNoVeiculos() throws Exception {
        Mockito.when(veiculoGateway.listar(0, 10))
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

        Mockito.verify(veiculoGateway).listar(0, 10);
    }

    @Test
    void shouldReturn200WithVeiculosMappedToResponseJson() throws Exception {
        var veiculo = Veiculo.reconstituir(1L, 2L, "ABC1234", "Gol", 2020);
        Mockito.when(veiculoGateway.listar(0, 10))
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

        Mockito.verify(veiculoGateway).listar(0, 10);
    }

    @Test
    void shouldReturn200WithDefaultPaginationWhenParamsOmitted() throws Exception {
        Mockito.when(veiculoGateway.listar(0, 10))
                .thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(MockMvcRequestBuilders.get("/veiculo"))
                .andExpect(status().isOk());

        Mockito.verify(veiculoGateway).listar(0, 10);
    }
}
