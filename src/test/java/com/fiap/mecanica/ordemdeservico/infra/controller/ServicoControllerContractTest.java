package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.usecase.AtualizarServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.CriarServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.DeletarServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ListarServicosUseCase;
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
@WebMvcTest(controllers = ServicoController.class)
class ServicoControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarServicoUseCase criarServicoUseCase;

    @MockitoBean
    private AtualizarServicoUseCase atualizarServicoUseCase;

    @MockitoBean
    private DeletarServicoUseCase deletarServicoUseCase;

    @MockitoBean
    private ListarServicosUseCase listarServicosUseCase;

    private static final String VALID_BODY =
            "{\"nome\":\"Troca de óleo\",\"descricao\":\"Troca com filtro incluso\",\"preco\":150.00}";

    @ParameterizedTest
    @CsvSource({
            "'{\"descricao\":\"Troca com filtro\",\"preco\":150.00}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Troca de óleo\",\"preco\":150.00}', descricao, 'A descrição deve ser preenchida'",
            "'{\"nome\":\"Troca de óleo\",\"descricao\":\"Troca com filtro\"}', preco, 'O preço deve ser preenchido'"
    })
    void shouldReturn400WhenRequiredFieldIsMissingOnCreate(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(criarServicoUseCase);
    }

    @Test
    void shouldReturn400WhenPrecoIsZeroOnCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Troca\",\"descricao\":\"Desc\",\"preco\":0.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.preco").value("O preço deve ser maior que zero"));
    }

    @Test
    void shouldReturn201WhenValidCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        Mockito.verify(criarServicoUseCase).criar(Mockito.any());
    }

    @Test
    void shouldReturn200WithEmptyPageWhenNoServicos() throws Exception {
        Mockito.when(listarServicosUseCase.listar(Mockito.any()))
                .thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(MockMvcRequestBuilders.get("/servico")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void shouldReturn200WithServicosMappedToResponseJson() throws Exception {
        var servico = Servico.reconstituir(1L, "Troca de óleo", "Desc", new BigDecimal("150.00"));
        Mockito.when(listarServicosUseCase.listar(Mockito.any()))
                .thenReturn(new Pagina<>(List.of(servico), 0, 10, 1L, 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/servico")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Troca de óleo"))
                .andExpect(jsonPath("$.content[0].preco").value(150.00))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturn204WhenValidUpdate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/servico/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNoContent());

        Mockito.verify(atualizarServicoUseCase).atualizar(Mockito.any());
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnUpdate() throws Exception {
        Mockito.doThrow(new ServicoNaoEncontradoException()).when(atualizarServicoUseCase).atualizar(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.put("/servico/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }

    @ParameterizedTest
    @CsvSource({
            "'{\"descricao\":\"Troca com filtro\",\"preco\":150.00}', nome, 'O nome deve ser preenchido'",
            "'{\"nome\":\"Troca de óleo\",\"descricao\":\"Troca com filtro\"}', preco, 'O preço deve ser preenchido'"
    })
    void shouldReturn400WhenRequiredFieldIsMissingOnUpdate(String requestJson, String field, String expectedMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/servico/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + field).value(expectedMessage));

        Mockito.verifyNoInteractions(atualizarServicoUseCase);
    }

    @Test
    void shouldReturn204WhenDeleteServicoSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/servico/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(deletarServicoUseCase).deletar(1L);
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnDelete() throws Exception {
        Mockito.doThrow(new ServicoNaoEncontradoException()).when(deletarServicoUseCase).deletar(99L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/servico/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Serviço não encontrado"));
    }
}
