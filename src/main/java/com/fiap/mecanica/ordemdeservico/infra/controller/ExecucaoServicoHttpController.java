package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
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
@Tag(name = "Execução de Serviços")
public class ExecucaoServicoHttpController {

    private final ExecucaoServicoCleanController cleanController;

    public ExecucaoServicoHttpController(OrdemDeServicoGateway ordemDeServicoGateway,
                                          ServicoGateway servicoGateway,
                                          NotificacaoGateway notificacaoGateway,
                                          MetricasGateway metricasGateway) {
        this.cleanController = new ExecucaoServicoCleanController(
                ordemDeServicoGateway, servicoGateway, notificacaoGateway, metricasGateway
        );
    }

    @Operation(summary = "Iniciar Serviço",
            description = "Inicia a execução de um serviço vinculado à ordem de serviço. " +
                    "A ordem precisa estar no status EM_EXECUCAO e o serviço ainda não pode ter sido iniciado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço iniciado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou serviço não encontrado"),
            @ApiResponse(responseCode = "422", description = "Ordem de serviço não está em execução, serviço não está vinculado ou já foi iniciado/finalizado")
    })
    @PatchMapping("/{ordemServicoId}/servicos/{servicoId}/iniciar")
    public ResponseEntity<Void> iniciarServico(@PathVariable Long ordemServicoId, @PathVariable Long servicoId) {
        cleanController.iniciarServico(ordemServicoId, servicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Finalizar Serviço",
            description = "Finaliza a execução de um serviço vinculado à ordem de serviço. O serviço precisa ter sido previamente iniciado. " +
                    "Quando todos os serviços da ordem forem finalizados, a ordem avança automaticamente para o status FINALIZADA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço finalizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil MECANICO"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço ou serviço não encontrado"),
            @ApiResponse(responseCode = "422", description = "Ordem de serviço não está em execução, serviço não está vinculado ou ainda não foi iniciado")
    })
    @PatchMapping("/{ordemServicoId}/servicos/{servicoId}/finalizar")
    public ResponseEntity<Void> finalizarServico(@PathVariable Long ordemServicoId, @PathVariable Long servicoId) {
        cleanController.finalizarServico(ordemServicoId, servicoId);
        return ResponseEntity.noContent().build();
    }
}
