package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ordem-servico")
@Tag(name = "Gestão de Itens")
public class VinculoOrdemDeServicoHttpController {

    private final VinculoOrdemDeServicoCleanController cleanController;

    public VinculoOrdemDeServicoHttpController(OrdemDeServicoGateway ordemDeServicoGateway,
                                                ServicoGateway servicoGateway,
                                                PecaGateway pecaGateway,
                                                InsumoGateway insumoGateway) {
        this.cleanController = new VinculoOrdemDeServicoCleanController(
                ordemDeServicoGateway, servicoGateway, pecaGateway, insumoGateway
        );
    }

    @Operation(summary = "Vincular Serviço",
            description = "Vincula um serviço à ordem de serviço. Somente permitido enquanto a ordem estiver no status RECEBIDA ou EM_DIAGNOSTICO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço vinculado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou serviço não encontrado"),
            @ApiResponse(responseCode = "422", description = "Serviço já está vinculado à ordem de serviço ou ordem de serviço não está em diagnóstico ou recebida")
    })
    @PutMapping("/{ordemServicoId}/servicos/{servicoId}")
    public ResponseEntity<Void> vincularServico(@PathVariable Long ordemServicoId, @PathVariable Long servicoId) {
        cleanController.vincularServico(ordemServicoId, servicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desvincular Serviço",
            description = "Remove o vínculo de um serviço da ordem de serviço. Somente permitido enquanto a ordem estiver no status RECEBIDA ou EM_DIAGNOSTICO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço desvinculado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou serviço não encontrado"),
            @ApiResponse(responseCode = "422", description = "Serviço não está vinculado à ordem de serviço ou ordem de serviço não está em diagnóstico ou recebida")
    })
    @DeleteMapping("/{ordemServicoId}/servicos/{servicoId}")
    public ResponseEntity<Void> desvincularServico(@PathVariable Long ordemServicoId, @PathVariable Long servicoId) {
        cleanController.desvincularServico(ordemServicoId, servicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Vincular Peça",
            description = "Vincula uma peça à ordem de serviço, baixando o estoque correspondente. Se a peça já estiver vinculada, soma a quantidade informada. Somente permitido enquanto a ordem estiver no status RECEBIDA ou EM_DIAGNOSTICO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Peça vinculada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou peça não encontrada"),
            @ApiResponse(responseCode = "422", description = "Estoque insuficiente para realizar a operação ou ordem de serviço não está em diagnóstico ou recebida")
    })
    @PutMapping("/{ordemServicoId}/pecas/{pecaId}")
    public ResponseEntity<Void> vincularPeca(@PathVariable Long ordemServicoId, @PathVariable Long pecaId,
                                             @RequestBody @Valid VincularPecaRequestJson request) {
        cleanController.vincularPeca(ordemServicoId, pecaId, request.quantidade());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desvincular Peça",
            description = "Remove ou subtrai a quantidade de uma peça vinculada à ordem de serviço, devolvendo o estoque correspondente. " +
                    "Se a quantidade informada for igual à vinculada, o vínculo é removido integralmente. Somente permitido enquanto a ordem estiver no status EM_DIAGNOSTICO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Peça desvinculada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou peça não encontrada"),
            @ApiResponse(responseCode = "422", description = "Ordem de serviço não está em diagnóstico, peça não está vinculada ou quantidade a desvincular é maior que a vinculada")
    })
    @DeleteMapping("/{ordemServicoId}/pecas/{pecaId}")
    public ResponseEntity<Void> desvincularPeca(@PathVariable Long ordemServicoId, @PathVariable Long pecaId,
                                                @RequestBody @Valid DesvincularPecaRequestJson request) {
        cleanController.desvincularPeca(ordemServicoId, pecaId, request.quantidade());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Vincular Insumo",
            description = "Vincula um insumo à ordem de serviço, baixando o estoque correspondente. " +
                    "Se o insumo já estiver vinculado, soma a quantidade informada. Somente permitido enquanto a ordem estiver no status RECEBIDA ou EM_DIAGNOSTICO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insumo vinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou insumo não encontrado"),
            @ApiResponse(responseCode = "422", description = "Estoque insuficiente para realizar a operação ou ordem de serviço não está em diagnóstico ou recebida")
    })
    @PutMapping("/{ordemServicoId}/insumos/{insumoId}")
    public ResponseEntity<Void> vincularInsumo(@PathVariable Long ordemServicoId, @PathVariable Long insumoId,
                                               @RequestBody @Valid VincularInsumoRequestJson request) {
        cleanController.vincularInsumo(ordemServicoId, insumoId, request.quantidade());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desvincular Insumo",
            description = "Remove ou subtrai a quantidade de um insumo vinculado à ordem de serviço, devolvendo o estoque correspondente. " +
                    "Se a quantidade informada for igual à vinculada, o vínculo é removido integralmente. Somente permitido enquanto a ordem estiver no status EM_DIAGNOSTICO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insumo desvinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou insumo não encontrado"),
            @ApiResponse(responseCode = "422", description = "Ordem de serviço não está em diagnóstico, insumo não está vinculado ou quantidade a desvincular é maior que a vinculada")
    })
    @DeleteMapping("/{ordemServicoId}/insumos/{insumoId}")
    public ResponseEntity<Void> desvincularInsumo(@PathVariable Long ordemServicoId, @PathVariable Long insumoId,
                                                   @RequestBody @Valid DesvincularInsumoRequestJson request) {
        cleanController.desvincularInsumo(ordemServicoId, insumoId, request.quantidade());
        return ResponseEntity.noContent().build();
    }
}
