package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.dto.CriarVeiculoDto;
import com.fiap.mecanica.gestao.core.usecase.CriarVeiculoUseCase;
import com.fiap.mecanica.gestao.infra.controller.json.VeiculoRequestJson;
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
@RequestMapping(value = "/veiculo")
@Tag(name = "veículo")
public class VeiculoController {

    private final CriarVeiculoUseCase criarVeiculoUseCase;

    @Operation(
            summary = "Criar um veiculo",
            description = "Cria um novo veiculo vinculado ao cliente. Somente um atendente pode criar um veiculo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Veiculo criado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Ja existe um Veiculo criado com a placa informada"),
            @ApiResponse(responseCode = "400", description = "Parametros de entrada invalidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<HttpStatus> criar(@RequestBody @Valid VeiculoRequestJson veiculoRequestJson) {
        var dto = new CriarVeiculoDto(
                veiculoRequestJson.clienteId(),
                veiculoRequestJson.placa(),
                veiculoRequestJson.modelo(),
                veiculoRequestJson.ano()
        );
        criarVeiculoUseCase.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
