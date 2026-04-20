package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.dto.CriarClienteDto;
import com.fiap.mecanica.gestao.core.usecase.CriarClienteUseCase;
import com.fiap.mecanica.gestao.infra.controller.json.ClienteRequestJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/cliente")
@Tag(name = "Cliente")
public class ClienteController {

	private final CriarClienteUseCase criarClienteUseCase;

	@Operation(
			summary = "Criar um cliente",
			description = "Cria um novo cliente. Somente um atendente pode criar um cliente"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Parametros de entrada invalidos"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Acesso negado")
	})
	@PostMapping
	public ResponseEntity<HttpStatus> criar(@RequestBody @Valid ClienteRequestJson clienteRequestJson) {
		var dto = new CriarClienteDto(clienteRequestJson.nome(), clienteRequestJson.sobrenome(), clienteRequestJson.cpf(), clienteRequestJson.cnpj());
		criarClienteUseCase.criar(dto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
