package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.dto.CriarOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.usecase.CriarOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.OrdemDeServicoRequestJson;
import com.fiap.mecanica.shared.exception.dto.ExceptionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/ordem-servico")
@Tag(name = "Ordem de Serviço")
public class OrdemDeServicoController {

    private final CriarOrdemDeServicoUseCase criarOrdemDeServicoUseCase;

    @Operation(
            summary = "Abrir Ordem de Serviço",
            description = "Abre uma nova Ordem de Serviço com status RECEBIDA. O atendente é identificado pelo token JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordem de Serviço criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Atendente, cliente ou veículo não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Veículo não pertence ao cliente informado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody @Valid OrdemDeServicoRequestJson request) {
        criarOrdemDeServicoUseCase.criar(new CriarOrdemDeServicoDto(request.clienteId(), request.veiculoId()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
