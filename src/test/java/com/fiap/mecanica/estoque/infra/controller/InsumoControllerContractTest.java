package com.fiap.mecanica.estoque.infra.controller;

import com.fiap.mecanica.estoque.core.usecase.CriarInsumoUseCase;
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
@WebMvcTest(controllers = InsumoController.class)
class InsumoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarInsumoUseCase criarInsumoUseCase;

    private static final String VALID_BODY =
            "{\"nome\":\"Óleo de motor\",\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"quantidadeEstoque\":10,\"unidadeMedida\":\"LITRO\"}";

    // -------------------------------------------------------------------------
    // Validações de campos obrigatórios
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "'{\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"quantidadeEstoque\":10,\"unidadeMedida\":\"LITRO\"}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Óleo\",\"preco\":45.90,\"quantidadeEstoque\":10,\"unidadeMedida\":\"LITRO\"}', descricao, 'A descrição deve ser preenchida'",
            "'{\"nome\":\"Óleo\",\"descricao\":\"Óleo 5W30\",\"quantidadeEstoque\":10,\"unidadeMedida\":\"LITRO\"}', preco, 'O preço deve ser preenchido'",
            "'{\"nome\":\"Óleo\",\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"unidadeMedida\":\"LITRO\"}', quantidadeEstoque, 'A quantidade em estoque deve ser preenchida'",
            "'{\"nome\":\"Óleo\",\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"quantidadeEstoque\":10}', unidadeMedida, 'A unidade de medida deve ser preenchida'"
    })
    void shouldReturn400WhenRequiredFieldIsMissing(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(criarInsumoUseCase);
    }

    @Test
    void shouldReturn400WhenPrecoIsZero() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Óleo\",\"descricao\":\"Óleo 5W30\",\"preco\":0.00,\"quantidadeEstoque\":10,\"unidadeMedida\":\"LITRO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.preco").value("O preço deve ser maior que zero"));

        Mockito.verifyNoInteractions(criarInsumoUseCase);
    }

    @Test
    void shouldReturn400WhenQuantidadeEstoqueIsNegative() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Óleo\",\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"quantidadeEstoque\":-1,\"unidadeMedida\":\"LITRO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantidadeEstoque").value("A quantidade em estoque não pode ser negativa"));

        Mockito.verifyNoInteractions(criarInsumoUseCase);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void shouldReturn201WhenValidRequestWithLitro() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        Mockito.verify(criarInsumoUseCase).criar(Mockito.any());
    }

    @Test
    void shouldReturn201WhenValidRequestWithMl() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Fluido de freio\",\"descricao\":\"DOT 4\",\"preco\":25.00,\"quantidadeEstoque\":500,\"unidadeMedida\":\"ML\"}"))
                .andExpect(status().isCreated());

        Mockito.verify(criarInsumoUseCase).criar(Mockito.any());
    }

    @Test
    void shouldReturn201WhenValidRequestWithUnidade() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Vela de ignição\",\"descricao\":\"NGK\",\"preco\":18.00,\"quantidadeEstoque\":20,\"unidadeMedida\":\"UNIDADE\"}"))
                .andExpect(status().isCreated());

        Mockito.verify(criarInsumoUseCase).criar(Mockito.any());
    }

    @Test
    void shouldReturn201WhenQuantidadeEstoqueIsZero() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Óleo\",\"descricao\":\"Óleo 5W30\",\"preco\":45.90,\"quantidadeEstoque\":0,\"unidadeMedida\":\"LITRO\"}"))
                .andExpect(status().isCreated());

        Mockito.verify(criarInsumoUseCase).criar(Mockito.any());
    }
}
