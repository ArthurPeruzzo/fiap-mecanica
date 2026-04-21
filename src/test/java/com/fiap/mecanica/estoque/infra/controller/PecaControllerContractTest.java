package com.fiap.mecanica.estoque.infra.controller;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.usecase.AtualizarPecaUseCase;
import com.fiap.mecanica.estoque.core.usecase.CriarPecaUseCase;
import com.fiap.mecanica.estoque.core.usecase.DeletarPecaUseCase;
import com.fiap.mecanica.estoque.core.usecase.ListarPecasUseCase;
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

import java.math.BigDecimal;
import java.util.List;

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

    @MockitoBean
    private AtualizarPecaUseCase atualizarPecaUseCase;

    @MockitoBean
    private DeletarPecaUseCase deletarPecaUseCase;

    @MockitoBean
    private ListarPecasUseCase listarPecasUseCase;

    private static final String VALID_BODY =
            "{\"nome\":\"Filtro de óleo\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":10}";

    // -------------------------------------------------------------------------
    // POST /peca
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "'{\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":10}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Filtro\",\"preco\":29.90,\"quantidadeEstoque\":10}', descricao, 'A descrição deve ser preenchida'",
            "'{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"quantidadeEstoque\":10}', preco, 'O preço deve ser preenchido'",
            "'{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90}', quantidadeEstoque, 'A quantidade em estoque deve ser preenchida'"
    })
    void shouldReturn400WhenRequiredFieldIsMissingOnCreate(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(criarPecaUseCase);
    }

    @Test
    void shouldReturn400WhenPrecoIsZeroOnCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"preco\":0.00,\"quantidadeEstoque\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.preco").value("O preço deve ser maior que zero"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNegativeOnCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantidadeEstoque").value("A quantidade em estoque não pode ser negativa"));
    }

    @Test
    void shouldReturn201WhenValidCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        Mockito.verify(criarPecaUseCase).criar(Mockito.any());
    }

    // -------------------------------------------------------------------------
    // GET /peca
    // -------------------------------------------------------------------------

    @Test
    void shouldReturn200WithEmptyPageWhenNoPecas() throws Exception {
        Mockito.when(listarPecasUseCase.listar(Mockito.any()))
                .thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(MockMvcRequestBuilders.get("/peca")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void shouldReturn200WithPecasMappedToResponseJson() throws Exception {
        var peca = Peca.reconstituir(1L, "Filtro", "Desc", new BigDecimal("29.90"), 10);
        Mockito.when(listarPecasUseCase.listar(Mockito.any()))
                .thenReturn(new Pagina<>(List.of(peca), 0, 10, 1L, 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/peca")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Filtro"))
                .andExpect(jsonPath("$.content[0].preco").value(29.90))
                .andExpect(jsonPath("$.content[0].quantidadeEstoque").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // -------------------------------------------------------------------------
    // PUT /peca/{id}
    // -------------------------------------------------------------------------

    @Test
    void shouldReturn204WhenValidUpdate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/peca/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNoContent());

        Mockito.verify(atualizarPecaUseCase).atualizar(Mockito.any());
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnUpdate() throws Exception {
        Mockito.doThrow(new PecaNaoEncontradaException()).when(atualizarPecaUseCase).atualizar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.put("/peca/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }

    @ParameterizedTest
    @CsvSource({
            "'{\"descricao\":\"Filtro 1.0\",\"preco\":29.90,\"quantidadeEstoque\":10}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Filtro\",\"descricao\":\"Filtro 1.0\",\"quantidadeEstoque\":10}', preco, 'O preço deve ser preenchido'"
    })
    void shouldReturn400WhenRequiredFieldIsMissingOnUpdate(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/peca/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(atualizarPecaUseCase);
    }

    // -------------------------------------------------------------------------
    // DELETE /peca/{id}
    // -------------------------------------------------------------------------

    @Test
    void shouldReturn204WhenDeletarPecaSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/peca/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(deletarPecaUseCase).deletar(1L);
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnDelete() throws Exception {
        Mockito.doThrow(new PecaNaoEncontradaException()).when(deletarPecaUseCase).deletar(99L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/peca/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Peça não encontrada"));
    }
}
