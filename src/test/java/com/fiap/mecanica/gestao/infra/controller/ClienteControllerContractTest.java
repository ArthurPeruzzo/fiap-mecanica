package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.usecase.AtualizarClienteUseCase;
import com.fiap.mecanica.gestao.core.usecase.CriarClienteUseCase;
import com.fiap.mecanica.gestao.core.usecase.DeletarClienteUseCase;
import com.fiap.mecanica.gestao.core.usecase.ListarClientesUseCase;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@ImportAutoConfiguration(NoSecurityConfiguration.class)
@WebMvcTest(controllers = ClienteController.class)
class ClienteControllerContractTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CriarClienteUseCase criarClienteUseCase;

	@MockitoBean
	private AtualizarClienteUseCase atualizarClienteUseCase;

	@MockitoBean
	private DeletarClienteUseCase deletarClienteUseCase;

	@MockitoBean
	private ListarClientesUseCase listarClientesUseCase;

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

		Mockito.verifyNoInteractions(criarClienteUseCase);
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

		Mockito.verifyNoInteractions(criarClienteUseCase);
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

		Mockito.verifyNoInteractions(criarClienteUseCase);
	}

	@Test
	void shouldReturn201WhenValidCpfProvided() throws Exception {
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

		Mockito.verify(criarClienteUseCase).criar(Mockito.any());
	}

	@Test
	void shouldReturn201WhenValidCnpjProvided() throws Exception {
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

		Mockito.verify(criarClienteUseCase).criar(Mockito.any());
	}

	@Test
	void shouldReturn200WithEmptyPageWhenNoClientes() throws Exception {
		Mockito.when(listarClientesUseCase.listar(Mockito.any()))
				.thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

		mockMvc.perform(MockMvcRequestBuilders.get("/cliente")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.totalElements").value(0))
				.andExpect(jsonPath("$.totalPages").value(0))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(10));

		Mockito.verify(listarClientesUseCase).listar(Mockito.any());
	}

	@Test
	void shouldReturn200WithClientesMappedToResponseJson() throws Exception {
		var cliente = Cliente.reconstituir(1L,"Pedro" , null, "12345678909");
		Mockito.when(listarClientesUseCase.listar(Mockito.any()))
				.thenReturn(new Pagina<>(List.of(cliente), 0, 10, 1L, 1));

		mockMvc.perform(MockMvcRequestBuilders.get("/cliente")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].nome").value("Pedro"))
				.andExpect(jsonPath("$.content[0].cpf").value("123.456.789-09"))
				.andExpect(jsonPath("$.content[0].cnpj").doesNotExist())
				.andExpect(jsonPath("$.totalElements").value(1));

		Mockito.verify(listarClientesUseCase).listar(Mockito.any());
	}

	@Test
	void shouldReturn200WithDefaultPaginationWhenParamsOmitted() throws Exception {
		Mockito.when(listarClientesUseCase.listar(Mockito.any()))
				.thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

		mockMvc.perform(MockMvcRequestBuilders.get("/cliente"))
				.andExpect(status().isOk());

		Mockito.verify(listarClientesUseCase).listar(Mockito.any());
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

		Mockito.verifyNoInteractions(atualizarClienteUseCase);
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

		Mockito.verifyNoInteractions(atualizarClienteUseCase);
	}

	@Test
	void shouldReturn204WhenValidCpfProvidedOnUpdate() throws Exception {
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

		Mockito.verify(atualizarClienteUseCase).atualizar(Mockito.any());
	}

	@Test
	void shouldReturn404WhenClienteNotFoundOnUpdate() throws Exception {
		String json = """
				{
				  "nome": "Pedro",
				  "cpf": "951.147.520-73"
				}
				""";

		Mockito.doThrow(new ClienteNaoEncontradoException())
				.when(atualizarClienteUseCase).atualizar(Mockito.any());

		mockMvc.perform(MockMvcRequestBuilders.put("/cliente/99")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn204WhenDeletarClienteSuccessfully() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.delete("/cliente/1"))
				.andExpect(status().isNoContent());

		Mockito.verify(deletarClienteUseCase).deletar(1L);
	}

	@Test
	void shouldReturn404WhenClienteNotFoundOnDelete() throws Exception {
		Mockito.doThrow(new ClienteNaoEncontradoException())
				.when(deletarClienteUseCase).deletar(99L);

		mockMvc.perform(MockMvcRequestBuilders.delete("/cliente/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Cliente não encontrado"));
	}
}
