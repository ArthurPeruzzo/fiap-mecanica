package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ordem-servico/orcamento")
@Tag(name = "Orçamento")
public class OrcamentoHttpController {

    private final OrcamentoCleanController cleanController;

    public OrcamentoHttpController(OrdemDeServicoGateway ordemDeServicoGateway,
                                    PecaGateway pecaGateway,
                                    InsumoGateway insumoGateway,
                                    NotificacaoGateway notificacaoGateway,
                                    LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway,
                                    @Value("${url.aprovar.orcamento}") String urlAprovarOrcamento,
                                    @Value("${url.recusar.orcamento}") String urlRecusarOrcamento) {
        this.cleanController = new OrcamentoCleanController(
                ordemDeServicoGateway, pecaGateway, insumoGateway,
                notificacaoGateway, linkAprovacaoOrcamentoGateway,
                urlAprovarOrcamento, urlRecusarOrcamento
        );
    }

    @Operation(summary = "Enviar orçamento da Ordem de Serviço",
            description = "Envia o orçamento ao cliente e avança o status para AGUARDANDO_APROVACAO. " +
                    "Gera um link de aprovação com validade de 3 dias que é enviado ao cliente via notificação. " +
                    "Somente permitido quando a ordem estiver no status DIAGNOSTICO_CONCLUIDO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orçamento enviado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
            @ApiResponse(responseCode = "422", description = "Status inválido para a operação")
    })
    @PostMapping("/envio/{ordemServicoId}")
    public ResponseEntity<Void> enviarOrcamento(@PathVariable Long ordemServicoId) {
        cleanController.enviarOrcamento(ordemServicoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Recusar orçamento da Ordem de Serviço",
            description = "O atendente registra a recusa do cliente e cancela a ordem de serviço, avançando o status para CANCELADA. " +
                    "Somente permitido quando a ordem estiver no status AGUARDANDO_APROVACAO. " +
                    "As quantidades de peças e insumos são devolvidas ao estoque.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orçamento recusado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
            @ApiResponse(responseCode = "422", description = "Status inválido para a operação")
    })
    @PostMapping("/recusar/{ordemServicoId}")
    public ResponseEntity<Void> recusar(@PathVariable Long ordemServicoId) {
        cleanController.recusarOrcamento(ordemServicoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Aprovar orçamento da Ordem de Serviço",
            description = "Registra a aprovação do cliente e libera a execução dos serviços, avançando o status para EM_EXECUCAO. " +
                    "Somente permitido quando a ordem estiver no status AGUARDANDO_APROVACAO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orçamento aprovado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
            @ApiResponse(responseCode = "422", description = "Status inválido para a operação")
    })
    @PostMapping("/aprovar/{ordemServicoId}")
    public ResponseEntity<Void> aprovar(@PathVariable Long ordemServicoId) {
        cleanController.aprovarOrcamento(ordemServicoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Recusar orçamento via link público",
            description = "Endpoint público (sem autenticação) acionado pelo cliente a partir do link recebido por notificação. " +
                    "Cancela a ordem de serviço avançando o status para CANCELADA e devolve peças e insumos ao estoque. " +
                    "O link expira em 3 dias após o envio do orçamento e só pode ser utilizado uma vez.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orçamento recusado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Link não encontrado"),
            @ApiResponse(responseCode = "410", description = "Link expirado ou já utilizado"),
            @ApiResponse(responseCode = "422", description = "Status inválido para a operação")
    })
    @GetMapping("/externo/recusar/{token}")
    public ResponseEntity<Void> recusarExterno(@PathVariable String token) {
        cleanController.recusarOrcamentoViaToken(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Aprovar orçamento via link público",
            description = "Endpoint público (sem autenticação) acionado pelo cliente a partir do link recebido por notificação. " +
                    "Registra a aprovação do orçamento e libera a execução dos serviços, avançando o status para EM_EXECUCAO. " +
                    "O link expira em 3 dias após o envio do orçamento e só pode ser utilizado uma vez.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orçamento aprovado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Link não encontrado"),
            @ApiResponse(responseCode = "410", description = "Link expirado ou já utilizado"),
            @ApiResponse(responseCode = "422", description = "Status inválido para a operação")
    })
    @GetMapping("/externo/aprovar/{token}")
    public ResponseEntity<Void> aprovarExterno(@PathVariable String token) {
        cleanController.aprovarOrcamentoViaToken(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
