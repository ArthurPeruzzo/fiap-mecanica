package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ordem-servico")
@Tag(name = "Diagnóstico")
public class DiagnosticoOrdemDeServicoHttpController {

    private final DiagnosticoOrdemDeServicoCleanController cleanController;

    public DiagnosticoOrdemDeServicoHttpController(OrdemDeServicoGateway ordemDeServicoGateway,
                                                    MecanicoGateway mecanicoGateway,
                                                    TokenGateway tokenGateway) {
        this.cleanController = new DiagnosticoOrdemDeServicoCleanController(
                ordemDeServicoGateway, mecanicoGateway, tokenGateway
        );
    }

    @Operation(summary = "Iniciar Diagnóstico",
            description = "Atribui o mecânico autenticado à ordem de serviço e avança o status para EM_DIAGNOSTICO. " +
                    "Somente permitido se a ordem estiver no status RECEBIDA e sem mecânico vinculado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou mecânico não encontrado"),
            @ApiResponse(responseCode = "422", description = "Outro mecânico já é responsável por esta ordem de serviço ou status inválido para a operação")
    })
    @PatchMapping("/{ordemServicoId}/diagnostico")
    public ResponseEntity<Void> iniciarDiagnostico(@PathVariable Long ordemServicoId) {
        cleanController.iniciarDiagnostico(ordemServicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Concluir Diagnóstico",
            description = "Conclui o diagnóstico, calcula o valor total do orçamento com base nos serviços, peças e insumos vinculados, " +
                    "e avança o status para DIAGNOSTICO_CONCLUIDO. Somente o mecânico responsável pela ordem pode concluir o diagnóstico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou mecânico não encontrado"),
            @ApiResponse(responseCode = "422", description = "Mecânico não é o responsável, nenhum serviço vinculado ou status inválido para a operação")
    })
    @PatchMapping("/{ordemServicoId}/diagnostico/conclusao")
    public ResponseEntity<Void> concluirDiagnostico(@PathVariable Long ordemServicoId) {
        cleanController.concluirDiagnostico(ordemServicoId);
        return ResponseEntity.noContent().build();
    }
}
