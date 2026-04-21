package com.fiap.mecanica.estoque.infra.controller;

import com.fiap.mecanica.estoque.core.dto.CriarInsumoDto;
import com.fiap.mecanica.estoque.core.usecase.CriarInsumoUseCase;
import com.fiap.mecanica.estoque.infra.controller.json.InsumoRequestJson;
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
@RequestMapping("/insumo")
@Tag(name = "Insumo")
public class InsumoController {

	private final CriarInsumoUseCase criarInsumoUseCase;

	@Operation(
			summary = "Criar um insumo",
			description = "Cria um novo insumo no estoque"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Insumo criado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Acesso negado")
	})
	@PostMapping
	public ResponseEntity<Void> criar(@RequestBody @Valid InsumoRequestJson request) {
		var dto = new CriarInsumoDto(request.nome(), request.descricao(), request.preco(),
				request.quantidadeEstoque(), request.unidadeMedida());
		criarInsumoUseCase.criar(dto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
