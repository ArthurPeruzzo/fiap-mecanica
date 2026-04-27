package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.dto.CriarOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ConcluirDiagnosticoOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.CriarOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.DesvincularServicoOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.IniciarDiagnosticoOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.VincularServicoOrdemDeServicoUseCase;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ordem-servico")
@Tag(name = "Ordem de Serviço")
public class OrdemDeServicoController {

    private final CriarOrdemDeServicoUseCase criarOrdemDeServicoUseCase;
    private final IniciarDiagnosticoOrdemDeServicoUseCase iniciarDiagnosticoOrdemDeServicoUseCase;
    private final ConcluirDiagnosticoOrdemDeServicoUseCase concluirDiagnosticoOrdemDeServicoUseCase;
    private final VincularServicoOrdemDeServicoUseCase vincularServicoOrdemDeServicoUseCase;
    private final DesvincularServicoOrdemDeServicoUseCase desvincularServicoOrdemDeServicoUseCase;

    @Operation(summary = "Abrir Ordem de Serviço",
            description = "Abre uma nova Ordem de Serviço com status RECEBIDA. A descrição registra o relato do cliente. O atendente é identificado pelo token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordem de Serviço criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Atendente, cliente ou veículo não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Veículo não pertence ao cliente informado ou já existe uma ordem de serviço aberta para este veículo",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody @Valid OrdemDeServicoRequestJson request) {
        criarOrdemDeServicoUseCase.criar(new CriarOrdemDeServicoDto(request.clienteId(), request.veiculoId(), request.descricao()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Iniciar Diagnóstico",
            description = "Atribui o mecânico autenticado à ordem de serviço e avança o status para EM_DIAGNOSTICO. " +
                    "Somente permitido se a ordem estiver no status RECEBIDA, sem mecânico vinculado ou com o mesmo mecânico já responsável.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou mecânico não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "409", description = "Ordem de serviço já está em diagnóstico",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Outro mecânico já é responsável por esta ordem de serviço ou status inválido para a operação",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PatchMapping("/{id}/diagnostico")
    public ResponseEntity<Void> iniciarDiagnostico(@PathVariable Long id) {
        iniciarDiagnosticoOrdemDeServicoUseCase.iniciarDiagnostico(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Concluir Diagnóstico",
            description = "Conclui o diagnóstico e avança o status para DIAGNOSTICO_CONCLUIDO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou mecânico não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Mecânico não é o responsável, nenhum serviço vinculado ou status inválido para a operação",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PatchMapping("/{ordemServicoId}/diagnostico/conclusao")
    public ResponseEntity<Void> concluirDiagnostico(@PathVariable Long ordemServicoId) {
        concluirDiagnosticoOrdemDeServicoUseCase.concluirDiagnostico(ordemServicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Vincular Serviço",
            description = "Vincula um serviço à ordem de serviço.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço vinculado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou serviço não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Serviço já está vinculado à ordem de serviço ou ordem de serviço não está em diagnóstico",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PutMapping("/{ordemServicoId}/servicos/{servicoId}")
    public ResponseEntity<Void> vincularServico(@PathVariable Long ordemServicoId, @PathVariable Long servicoId) {
        vincularServicoOrdemDeServicoUseCase.vincular(ordemServicoId, servicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desvincular Serviço",
            description = "Remove o vínculo de um serviço da ordem de serviço.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço desvinculado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou serviço não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Serviço não está vinculado à ordem de serviço ou ordem de serviço não está em diagnóstico",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @DeleteMapping("/{ordemServicoId}/servicos/{servicoId}")
    public ResponseEntity<Void> desvincularServico(@PathVariable Long ordemServicoId, @PathVariable Long servicoId) {
        desvincularServicoOrdemDeServicoUseCase.desvincular(ordemServicoId, servicoId);
        return ResponseEntity.noContent().build();
    }
}
