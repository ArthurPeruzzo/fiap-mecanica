package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.dto.CriarOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.dto.InsumoVinculadoCriarDto;
import com.fiap.mecanica.ordemdeservico.core.dto.OrdemDeServicoListagemDto;
import com.fiap.mecanica.ordemdeservico.core.dto.PecaVinculadaCriarDto;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.StatusOrdemDeServicoResponseJson;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.*;
import com.fiap.mecanica.shared.page.PageResponse;
import com.fiap.mecanica.shared.page.Pagina;
import io.swagger.v3.oas.annotations.Operation;
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
    private final OrcamentoRecusadoViaAtendenteUseCase orcamentoRecusadoViaAtendenteUseCase;
    private final OrcamentoRecusadoViaTokenUseCase orcamentoRecusadoViaTokenUseCase;
    private final OrcamentoAprovadoViaAtendenteUseCase orcamentoAprovadoViaAtendenteUseCase;
    private final OrcamentoAprovadoViaTokenUseCase orcamentoAprovadoViaTokenUseCase;
    private final IniciarServicoOrdemDeServicoUseCase iniciarServicoOrdemDeServicoUseCase;
    private final FinalizarServicoOrdemDeServicoUseCase finalizarServicoOrdemDeServicoUseCase;
    private final EntregarOrdemDeServicoUseCase entregarOrdemDeServicoUseCase;
    private final ListarOrdemDeServicoUseCase listarOrdemDeServicoUseCase;
    private final ConsultarStatusOrdemDeServicoUseCase consultarStatusOrdemDeServicoUseCase;

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

        Long ordemDeServicoId = criarOrdemDeServicoUseCase.criar(new CriarOrdemDeServicoDto(
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
        iniciarDiagnosticoOrdemDeServicoUseCase.iniciarDiagnostico(ordemServicoId);
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
        concluirDiagnosticoOrdemDeServicoUseCase.concluirDiagnostico(ordemServicoId);
        return ResponseEntity.noContent().build();
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
        vincularServicoOrdemDeServicoUseCase.vincular(ordemServicoId, servicoId);
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
        desvincularServicoOrdemDeServicoUseCase.desvincular(ordemServicoId, servicoId);
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
        vincularPecaOrdemDeServicoUseCase.vincular(ordemServicoId, pecaId, request.quantidade());
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
        desvincularPecaOrdemDeServicoUseCase.desvincular(ordemServicoId, pecaId, request.quantidade());
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
        vincularInsumoOrdemDeServicoUseCase.vincular(ordemServicoId, insumoId, request.quantidade());
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
        desvincularInsumoOrdemDeServicoUseCase.desvincular(ordemServicoId, insumoId, request.quantidade());
        return ResponseEntity.noContent().build();
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
    @PostMapping("/orcamento/envio/{ordemServicoId}")
    public ResponseEntity<Void> enviarOrcamento(@PathVariable Long ordemServicoId) {
        enviarOrcamentoOrdemDeServicoUseCase.enviar(ordemServicoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Recusar orçamento da Ordem de Serviço",
            description = "O atendente registra a recusa do cliente e cancela a ordem de serviço, avançando o status para CANCELADA. Somente permitido quando a ordem estiver no status AGUARDANDO_APROVACAO. " +
                    "As quantidades de peças e insumos são devolvidas ao estoque, mas o vinculo na ordem de serviço continua para rastreabilidade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orçamento recusado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
            @ApiResponse(responseCode = "422", description = "Status inválido para a operação")
    })
    @PostMapping("/orcamento/recusar/{ordemServicoId}")
    public ResponseEntity<Void> recusar(@PathVariable Long ordemServicoId) {
        orcamentoRecusadoViaAtendenteUseCase.recusar(ordemServicoId);
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
            @ApiResponse(responseCode = "422", description = "Status inválido para a operação — ordem de serviço não está em AGUARDANDO_APROVACAO")
    })
    @GetMapping("/orcamento/externo/recusar/{token}")
    public ResponseEntity<Void> recusarExterno(@PathVariable String token) {
        orcamentoRecusadoViaTokenUseCase.recusar(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Aprovar orçamento da Ordem de Serviço",
            description = "Registra a aprovação do cliente e libera a execução dos serviços, avançando o status para EM_EXECUCAO. Somente permitido quando a ordem estiver no status AGUARDANDO_APROVACAO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orçamento aprovado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ATENDENTE"),
            @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
            @ApiResponse(responseCode = "422", description = "Status inválido para a operação")
    })
    @PostMapping("/orcamento/aprovar/{ordemServicoId}")
    public ResponseEntity<Void> aprovar(@PathVariable Long ordemServicoId) {
        orcamentoAprovadoViaAtendenteUseCase.aprovar(ordemServicoId);
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
            @ApiResponse(responseCode = "422", description = "Status inválido para a operação — ordem de serviço não está em AGUARDANDO_APROVACAO")
    })
    @GetMapping("/orcamento/externo/aprovar/{token}")
    public ResponseEntity<Void> aprovarExterno(@PathVariable String token) {
        orcamentoAprovadoViaTokenUseCase.aprovar(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
        iniciarServicoOrdemDeServicoUseCase.iniciar(ordemServicoId, servicoId);
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
        finalizarServicoOrdemDeServicoUseCase.finalizar(ordemServicoId, servicoId);
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
        entregarOrdemDeServicoUseCase.entregar(ordemServicoId);
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
        Pagina<OrdemDeServicoListagemDto> pagina = listarOrdemDeServicoUseCase.listar(page, size);
        return ResponseEntity.ok(PageResponse.from(pagina.map(OrdemDeServicoResponseJson::from)));
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
        return ResponseEntity.ok(StatusOrdemDeServicoResponseJson.from(
                consultarStatusOrdemDeServicoUseCase.consultar(ordemServicoId)));
    }
}
