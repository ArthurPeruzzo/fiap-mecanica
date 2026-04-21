package com.fiap.mecanica.estoque.infra.controller;

import com.fiap.mecanica.estoque.core.dto.CriarPecaDto;
import com.fiap.mecanica.estoque.core.usecase.CriarPecaUseCase;
import com.fiap.mecanica.estoque.infra.controller.json.PecaRequestJson;
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
@RequestMapping("/peca")
@Tag(name = "Peça")
public class PecaController {

	private final CriarPecaUseCase criarPecaUseCase;

	@Operation(
			summary = "Criar uma peça",
			description = "Cria uma nova peça no estoque"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Peça criada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Acesso negado")
	})
	@PostMapping
	public ResponseEntity<Void> criar(@RequestBody @Valid PecaRequestJson request) {
		criarPecaUseCase.criar(new CriarPecaDto(request.nome(), request.descricao(), request.preco(), request.quantidadeEstoque()));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
