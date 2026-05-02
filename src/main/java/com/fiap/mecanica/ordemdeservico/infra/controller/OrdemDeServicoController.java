package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.dto.CriarOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.*;
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
    private final VincularPecaOrdemDeServicoUseCase vincularPecaOrdemDeServicoUseCase;
    private final DesvincularPecaOrdemDeServicoUseCase desvincularPecaOrdemDeServicoUseCase;
    private final VincularInsumoOrdemDeServicoUseCase vincularInsumoOrdemDeServicoUseCase;
    private final DesvincularInsumoOrdemDeServicoUseCase desvincularInsumoOrdemDeServicoUseCase;
    private final EnviarOrcamentoOrdemDeServicoUseCase enviarOrcamentoOrdemDeServicoUseCase;
    private final OrcamentoRecusadoOrdemDeServicoUseCase orcamentoRecusadoOrdemDeServicoUseCase;
    private final OrcamentoAprovadoOrdemDeServicoUseCase orcamentoAprovadoOrdemDeServicoUseCase;
    private final IniciarServicoOrdemDeServicoUseCase iniciarServicoOrdemDeServicoUseCase;
    private final FinalizarServicoOrdemDeServicoUseCase finalizarServicoOrdemDeServicoUseCase;
    private final EntregarOrdemDeServicoUseCase entregarOrdemDeServicoUseCase;

    @Operation(summary = "Criar Ordem de Serviço",
            description = "Cria uma nova Ordem de Serviço com status RECEBIDA. A descrição registra o relato do cliente. O atendente é identificado pelo token JWT.")
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

    @Operation(summary = "Vincular Peça",
            description = "Vincula uma peça à ordem de serviço.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Peça vinculada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou peça não encontrada",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Estoque insuficiente para realizar a operação ou ordem de serviço não está em diagnóstico",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PutMapping("/{ordemServicoId}/pecas/{pecaId}")
    public ResponseEntity<Void> vincularPeca(@PathVariable Long ordemServicoId, @PathVariable Long pecaId,
                                             @RequestBody @Valid VincularPecaRequestJson request) {
        vincularPecaOrdemDeServicoUseCase.vincular(ordemServicoId, pecaId, request.quantidade());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desvincular Peça",
            description = "Remove ou subtrai a quantidade de uma peça vinculada à ordem de serviço, devolvendo o estoque correspondente. " +
                    "Se a quantidade informada for igual à vinculada, o vínculo é removido integralmente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Peça desvinculada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou peça não encontrada",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Ordem de serviço não está em diagnóstico, peça não está vinculada ou quantidade a desvincular é maior que a vinculada",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @DeleteMapping("/{ordemServicoId}/pecas/{pecaId}")
    public ResponseEntity<Void> desvincularPeca(@PathVariable Long ordemServicoId, @PathVariable Long pecaId,
                                             @RequestBody @Valid DesvincularPecaRequestJson request) {
        desvincularPecaOrdemDeServicoUseCase.desvincular(ordemServicoId, pecaId, request.quantidade());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Vincular Insumo",
            description = "Vincula um insumo à ordem de serviço, baixando o estoque correspondente. " +
                    "Se o insumo já estiver vinculado, soma a quantidade informada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insumo vinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou insumo não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Estoque insuficiente para realizar a operação ou ordem de serviço não está em diagnóstico",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PutMapping("/{ordemServicoId}/insumos/{insumoId}")
    public ResponseEntity<Void> vincularInsumo(@PathVariable Long ordemServicoId, @PathVariable Long insumoId,
                                               @RequestBody @Valid VincularInsumoRequestJson request) {
        vincularInsumoOrdemDeServicoUseCase.vincular(ordemServicoId, insumoId, request.quantidade());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desvincular Insumo",
            description = "Remove ou subtrai a quantidade de um insumo vinculado à ordem de serviço, devolvendo o estoque correspondente. " +
                    "Se a quantidade informada for igual à vinculada, o vínculo é removido integralmente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insumo desvinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou insumo não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Ordem de serviço não está em diagnóstico, insumo não está vinculado ou quantidade a desvincular é maior que a vinculada",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @DeleteMapping("/{ordemServicoId}/insumos/{insumoId}")
    public ResponseEntity<Void> desvincularInsumo(@PathVariable Long ordemServicoId, @PathVariable Long insumoId,
                                                @RequestBody @Valid DesvincularInsumoRequestJson request) {
        desvincularInsumoOrdemDeServicoUseCase.desvincular(ordemServicoId, insumoId, request.quantidade());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Enviar orçamento da Ordem de Serviço",
            description = "Envia o orçamento para o cliente. Após isso o status passa a ser 'AGUARDANDO_APROVACAO' ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orçamento enviado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "status inválido para a operação",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping("/orcamento/envio/{ordemServicoId}")
    public ResponseEntity<Void> enviarOrcamento(@PathVariable Long ordemServicoId) {
        enviarOrcamentoOrdemDeServicoUseCase.enviar(ordemServicoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Recusar orçamento da Ordem de Serviço",
            description = "Após o cliente recusar o orçamento a ordem de servico deve ser cancelada. Após isso o status passa a ser 'CANCELADA' ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orçamento recusado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "status inválido para a operação",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping("/orcamento/recusar/{ordemServicoId}")
    public ResponseEntity<Void> recusar(@PathVariable Long ordemServicoId) {
        orcamentoRecusadoOrdemDeServicoUseCase.recursar(ordemServicoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Aprovar orçamento da Ordem de Serviço",
            description = "Após o cliente aprovar o orçamento a ordem de servico deve ser executada. Após isso o status passa a ser 'EM_EXECUCAO' ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orçamento aprovado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "status inválido para a operação",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping("/orcamento/aprovar/{ordemServicoId}")
    public ResponseEntity<Void> aprovar(@PathVariable Long ordemServicoId) {
        orcamentoAprovadoOrdemDeServicoUseCase.aprovar(ordemServicoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Iniciar Servico",
            description = "Inicia o serviço da Ordem de Serviço.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço iniciado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço, serviço ou mecânico não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Ordem de serviço não está em execução, serviço não está vinculado ou já foi iniciado/finalizado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PatchMapping("/{ordemServicoId}/servicos/{servicoId}/iniciar")
    public ResponseEntity<Void> iniciarServico(@PathVariable Long ordemServicoId, @PathVariable Long servicoId) {
        iniciarServicoOrdemDeServicoUseCase.iniciar(ordemServicoId, servicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Finalizar Servico",
            description = "Finaliza o serviço da Ordem de Serviço.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço finalizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço, serviço ou mecânico não encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "Ordem de serviço não está em execução, serviço não está vinculado ou já foi finalizado",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PatchMapping("/{ordemServicoId}/servicos/{servicoId}/finalizar")
    public ResponseEntity<Void> finalizarServico(@PathVariable Long ordemServicoId, @PathVariable Long servicoId) {
        finalizarServicoOrdemDeServicoUseCase.finalizar(ordemServicoId, servicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Entregar Ordem de Serviço",
            description = "Entrega a Ordem de Serviço.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ordem de serviço entregue com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil Atendente"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "422", description = "status inválido para a operação",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PatchMapping("/{ordemServicoId}/entregar")
    public ResponseEntity<Void> entregar(@PathVariable Long ordemServicoId) {
        entregarOrdemDeServicoUseCase.entregar(ordemServicoId);
        return ResponseEntity.noContent().build();
    }
}
