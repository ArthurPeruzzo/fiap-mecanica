package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.usecase.CriarClienteUseCase;
import com.fiap.mecanica.gestao.core.usecase.ListarClientesUseCase;
import com.fiap.mecanica.resources.NoSecurityConfiguration;
import com.fiap.mecanica.shared.page.Pagina;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
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
@WebMvcTest(controllers = ClienteController.class)
class ClienteControllerContractTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CriarClienteUseCase criarClienteUseCase;

	@MockitoBean
	private ListarClientesUseCase listarClientesUseCase;

	// -------------------------------------------------------------------------
	// Validações de campos obrigatórios e formato
	// -------------------------------------------------------------------------

	@ParameterizedTest
	@CsvSource({
			"'{\"nome\":\"\",\"sobrenome\":\"Silva\",\"cpf\":\"951.147.520-73\"}', nome, 'O nome deve ser preenchido'",
			"'{\"nome\":\"Pedro\",\"sobrenome\":\"\",\"cpf\":\"951.147.520-73\"}', sobrenome, 'O sobrenome deve ser preenchido'",
			"'{\"nome\":\"Pedro\",\"sobrenome\":\"Silva\",\"cpf\":\"000.000.000-00\"}', cpf, 'O conteúdo ou a formatação do CPF não é válida'",
			"'{\"nome\":\"Pedro\",\"sobrenome\":\"Silva\",\"cnpj\":\"00.000.000/0000-00\"}', cnpj, 'O conteúdo ou a formatação do CNPJ não é válida. Segue formatos de exemplo: AA.AAA.AAA/AAAA-DV ou 00.000.000/0000-00 com ou sem formatação'"
	})
	void shouldReturn400WithFieldValidationMessage(String requestJson, String field, String expectedMessage) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/cliente")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$." + field).value(expectedMessage));

		Mockito.verifyNoInteractions(criarClienteUseCase);
	}

	// -------------------------------------------------------------------------
	// Validação de classe: DocumentoValido
	// -------------------------------------------------------------------------

	@Test
	void shouldReturn400WhenBothCpfAndCnpjProvided() throws Exception {
		String json = """
				{
				  "nome": "Pedro",
				  "sobrenome": "Silva",
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
				  "nome": "Pedro",
				  "sobrenome": "Silva"
				}
				""";

		mockMvc.perform(MockMvcRequestBuilders.post("/cliente")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.clienteRequestJson").value("Informe CPF ou CNPJ, não ambos e não nenhum"));

		Mockito.verifyNoInteractions(criarClienteUseCase);
	}

	// -------------------------------------------------------------------------
	// Happy path
	// -------------------------------------------------------------------------

	@Test
	void shouldReturn201WhenValidCpfProvided() throws Exception {
		String json = """
				{
				  "nome": "Pedro",
				  "sobrenome": "Silva",
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
				  "sobrenome": "LTDA",
				  "cnpj": "9B.X1W.34S/0001-44"
				}
				""";

		mockMvc.perform(MockMvcRequestBuilders.post("/cliente")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isCreated());

		Mockito.verify(criarClienteUseCase).criar(Mockito.any());
	}

	// -------------------------------------------------------------------------
	// GET /cliente
	// -------------------------------------------------------------------------

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
		var cliente = Cliente.reconstituir(1L, new NomeCompleto("Pedro", "Silva"), null, "12345678909");
		Mockito.when(listarClientesUseCase.listar(Mockito.any()))
				.thenReturn(new Pagina<>(List.of(cliente), 0, 10, 1L, 1));

		mockMvc.perform(MockMvcRequestBuilders.get("/cliente")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].nome").value("Pedro"))
				.andExpect(jsonPath("$.content[0].sobrenome").value("Silva"))
				.andExpect(jsonPath("$.content[0].cpf").value("12345678909"))
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
}
