package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.dto.InsumoVinculadoCriarDto;
import com.fiap.mecanica.ordemdeservico.core.dto.PecaVinculadaCriarDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.*;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.page.PageResponse;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ordem-servico")
@Tag(name = "Ordem de Serviço")
public class OrdemDeServicoHttpController {

    private final OrdemDeServicoCleanController cleanController;

    public OrdemDeServicoHttpController(OrdemDeServicoGateway ordemDeServicoGateway,
                                         AtendenteGateway atendenteGateway,
                                         TokenGateway tokenGateway,
                                         VeiculoGateway veiculoGateway,
                                         ClienteGateway clienteGateway,
                                         MecanicoGateway mecanicoGateway,
                                         ServicoGateway servicoGateway,
                                         PecaGateway pecaGateway,
                                         InsumoGateway insumoGateway,
                                         NotificacaoGateway notificacaoGateway) {
        this.cleanController = new OrdemDeServicoCleanController(
                ordemDeServicoGateway, atendenteGateway, tokenGateway,
                veiculoGateway, clienteGateway, mecanicoGateway,
                servicoGateway, pecaGateway, insumoGateway,
                notificacaoGateway
        );
    }

    @Operation(summary = "Criar Ordem de Serviço",
            description = "Cria uma nova Ordem de Serviço com status RECEBIDA. A descrição registra o relato do cliente. O atendente é identificado pelo token JWT. " +
                    "Opcionalmente, é possível informar serviços, peças e insumos a vincular na criação; todos os campos de vínculo são opcionais e podem ser omitidos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordem de Serviço criada com sucesso. O corpo da resposta contém o ID da ordem criada"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Atendente, cliente, veículo, serviço, peça ou insumo não encontrado"),
            @ApiResponse(responseCode = "422", description = "Veículo não pertence ao cliente informado, já existe uma ordem de serviço aberta para este veículo, ou estoque insuficiente para uma das peças ou insumos informados")
    })
    @PostMapping
    public ResponseEntity<Long> criar(@RequestBody @Valid OrdemDeServicoRequestJson request) {
        var pecas = request.pecas() == null ? null :
                request.pecas().stream().map(p -> new PecaVinculadaCriarDto(p.id(), p.quantidade())).toList();
        var insumos = request.insumos() == null ? null :
                request.insumos().stream().map(i -> new InsumoVinculadoCriarDto(i.id(), i.quantidade())).toList();

        Long ordemDeServicoId = cleanController.criar(new CriarOrdemDeServicoDto(
                request.clienteId(), request.veiculoId(), request.servicosIds(), pecas, insumos, request.descricao()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(ordemDeServicoId);
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
            description = "Conclui o diagnóstico, calcula o valor total do orçamento com base nos serviços, peças e insumos vinculados, e avança o status para DIAGNOSTICO_CONCLUIDO. Somente o mecânico responsável pela ordem pode concluir o diagnóstico.")
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

    @Operation(summary = "Iniciar Serviço",
            description = "Inicia a execução de um serviço vinculado à ordem de serviço. A ordem precisa estar no status EM_EXECUCAO e o serviço ainda não pode ter sido iniciado.")
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
            description = "Finaliza a execução de um serviço vinculado à ordem de serviço. O serviço precisa ter sido previamente iniciado. Quando todos os serviços da ordem forem finalizados, a ordem avança automaticamente para o status FINALIZADA.")
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

    @Operation(summary = "Entregar Ordem de Serviço",
            description = "Entrega a Ordem de Serviço. A ordem de serviço só será entregue se o status estiver em 'FINALIZADA'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ordem de serviço entregue com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada"),
            @ApiResponse(responseCode = "422", description = "status inválido para a operação")
    })
    @PatchMapping("/{ordemServicoId}/entregar")
    public ResponseEntity<Void> entregar(@PathVariable Long ordemServicoId) {
        cleanController.entregar(ordemServicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar ordens de serviços",
            description = "Retorna a lista paginada de ordens de serviços. Somente perfil de ADMINISTRADOR tem permissão para listar"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ADMINISTRADOR")
    })
    @GetMapping("/detalhamento")
    public ResponseEntity<PageResponse<OrdemDeServicoResponseJson>> detalhamento(@RequestParam(defaultValue = "0") int page,
                                                                                  @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cleanController.listar(page, size));
    }

    @Operation(summary = "Consultar status da Ordem de Serviço",
            description = "Retorna o status atual de uma ordem de serviço pelo ID. Acessível por qualquer perfil autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    @GetMapping("/{ordemServicoId}/status")
    public ResponseEntity<StatusOrdemDeServicoResponseJson> consultarStatus(@PathVariable Long ordemServicoId) {
        return ResponseEntity.ok(cleanController.consultarStatus(ordemServicoId));
    }
}
