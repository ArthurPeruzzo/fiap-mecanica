package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
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
@WebMvcTest(controllers = ClienteHttpController.class)
class ClienteControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteGateway clienteGateway;

    @ParameterizedTest
    @CsvSource({
            "'{\"nome\":\"\",\"cpf\":\"951.147.520-73\"}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Pedro\",\"cpf\":\"000.000.000-00\"}', cpf, 'O conteúdo ou a formatação do CPF não é válida'",
            "'{\"nome\":\"Pedro\",\"cnpj\":\"00.000.000/0000-00\"}', cnpj, 'O conteúdo ou a formatação do CNPJ não é válida. Segue formatos de exemplo: AA.AAA.AAA/AAAA-DV ou 00.000.000/0000-00 com ou sem formatação'"
    })
    void shouldReturn400WithFieldValidationMessage(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(clienteGateway);
    }

    @Test
    void shouldReturn400WhenBothCpfAndCnpjProvided() throws Exception {
        String json = """
                {
                  "nome": "Pedro",
                  "cpf": "951.147.520-73",
                  "cnpj": "1A.3BC.45D/0001-EF"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.clienteRequestJson").value("Informe CPF ou CNPJ, não ambos e não nenhum"));

        Mockito.verifyNoInteractions(clienteGateway);
    }

    @Test
    void shouldReturn400WhenNeitherCpfNorCnpjProvided() throws Exception {
        String json = """
                {
                  "nome": "Pedro"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.clienteRequestJson").value("Informe CPF ou CNPJ, não ambos e não nenhum"));

        Mockito.verifyNoInteractions(clienteGateway);
    }

    @Test
    void shouldReturn201WhenValidCpfProvided() throws Exception {
        Mockito.when(clienteGateway.existePorCpf(Mockito.any())).thenReturn(false);

        String json = """
                {
                  "nome": "Pedro",
                  "cpf": "951.147.520-73"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        Mockito.verify(clienteGateway).criar(Mockito.any());
    }

    @Test
    void shouldReturn201WhenValidCnpjProvided() throws Exception {
        Mockito.when(clienteGateway.existePorCnpj(Mockito.any())).thenReturn(false);

        String json = """
                {
                  "nome": "Empresa",
                  "cnpj": "9B.X1W.34S/0001-44"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        Mockito.verify(clienteGateway).criar(Mockito.any());
    }

    @Test
    void shouldReturn200WithEmptyPageWhenNoClientes() throws Exception {
        Mockito.when(clienteGateway.listar(0, 10)).thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(MockMvcRequestBuilders.get("/cliente")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));

        Mockito.verify(clienteGateway).listar(0, 10);
    }

    @Test
    void shouldReturn200WithClientesMappedToResponseJson() throws Exception {
        var cliente = Cliente.reconstituir(1L, "Pedro", null, "12345678909");
        Mockito.when(clienteGateway.listar(0, 10)).thenReturn(new Pagina<>(List.of(cliente), 0, 10, 1L, 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/cliente")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Pedro"))
                .andExpect(jsonPath("$.content[0].cpf").value("123.456.789-09"))
                .andExpect(jsonPath("$.content[0].cnpj").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));

        Mockito.verify(clienteGateway).listar(0, 10);
    }

    @Test
    void shouldReturn200WithDefaultPaginationWhenParamsOmitted() throws Exception {
        Mockito.when(clienteGateway.listar(0, 10)).thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(MockMvcRequestBuilders.get("/cliente"))
                .andExpect(status().isOk());

        Mockito.verify(clienteGateway).listar(0, 10);
    }

    @ParameterizedTest
    @CsvSource({
            "'{\"nome\":\"\",\"cpf\":\"951.147.520-73\"}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Pedro\",\"cpf\":\"000.000.000-00\"}', cpf, 'O conteúdo ou a formatação do CPF não é válida'",
            "'{\"nome\":\"Pedro\",\"cnpj\":\"00.000.000/0000-00\"}', cnpj, 'O conteúdo ou a formatação do CNPJ não é válida. Segue formatos de exemplo: AA.AAA.AAA/AAAA-DV ou 00.000.000/0000-00 com ou sem formatação'"
    })
    void shouldReturn400WithFieldValidationMessageOnUpdate(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/cliente/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(clienteGateway);
    }

    @Test
    void shouldReturn400WhenBothCpfAndCnpjProvidedOnUpdate() throws Exception {
        String json = """
                {
                  "nome": "Pedro",
                  "cpf": "951.147.520-73",
                  "cnpj": "1A.3BC.45D/0001-EF"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/cliente/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.clienteRequestJson").value("Informe CPF ou CNPJ, não ambos e não nenhum"));

        Mockito.verifyNoInteractions(clienteGateway);
    }

    @Test
    void shouldReturn204WhenValidCpfProvidedOnUpdate() throws Exception {
        var cliente = Cliente.reconstituir(1L, "Pedro", null, "95114752073");
        Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        Mockito.when(clienteGateway.existePorCpfExcluindoId(Mockito.any(), Mockito.eq(1L))).thenReturn(false);

        String json = """
                {
                  "nome": "Pedro",
                  "cpf": "951.147.520-73"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/cliente/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        Mockito.verify(clienteGateway).atualizar(Mockito.any());
    }

    @Test
    void shouldReturn404WhenClienteNotFoundOnUpdate() throws Exception {
        Mockito.when(clienteGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        String json = """
                {
                  "nome": "Pedro",
                  "cpf": "951.147.520-73"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/cliente/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn204WhenDeletarClienteSuccessfully() throws Exception {
        var cliente = Cliente.reconstituir(1L, "Pedro", null, "12345678909");
        Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(cliente));

        mockMvc.perform(MockMvcRequestBuilders.delete("/cliente/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(clienteGateway).deletar(1L);
    }

    @Test
    void shouldReturn404WhenClienteNotFoundOnDelete() throws Exception {
        Mockito.when(clienteGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/cliente/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }
}
