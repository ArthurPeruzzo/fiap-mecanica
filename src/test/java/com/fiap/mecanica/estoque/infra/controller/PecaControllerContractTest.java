package com.fiap.mecanica.estoque.infra.controller;

import com.fiap.mecanica.estoque.core.usecase.CriarPecaUseCase;
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
@WebMvcTest(controllers = PecaController.class)
class PecaControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarPecaUseCase criarPecaUseCase;

    // -------------------------------------------------------------------------
    // Validações de campos obrigatórios
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "'{\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":10}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Filtro\",\"preco\":29.90,\"quantidadeEstoque\":10}', descricao, 'A descrição deve ser preenchida'",
            "'{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"quantidadeEstoque\":10}', preco, 'O preço deve ser preenchido'",
            "'{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90}', quantidadeEstoque, 'A quantidade em estoque deve ser preenchida'"
    })
    void shouldReturn400WhenRequiredFieldIsMissing(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(criarPecaUseCase);
    }

    @Test
    void shouldReturn400WhenPrecoIsZero() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"preco\":0.00,\"quantidadeEstoque\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.preco").value("O preço deve ser maior que zero"));

        Mockito.verifyNoInteractions(criarPecaUseCase);
    }

    @Test
    void shouldReturn400WhenQuantidadeEstoqueIsNegative() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantidadeEstoque").value("A quantidade em estoque não pode ser negativa"));

        Mockito.verifyNoInteractions(criarPecaUseCase);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void shouldReturn201WhenValidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Filtro de óleo\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":10}"))
                .andExpect(status().isCreated());

        Mockito.verify(criarPecaUseCase).criar(Mockito.any());
    }

    @Test
    void shouldReturn201WhenQuantidadeEstoqueIsZero() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Filtro de óleo\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":0}"))
                .andExpect(status().isCreated());

        Mockito.verify(criarPecaUseCase).criar(Mockito.any());
    }
}
