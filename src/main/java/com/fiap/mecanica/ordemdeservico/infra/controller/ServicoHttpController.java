package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.dto.AtualizarServicoDto;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarServicoDto;
import com.fiap.mecanica.ordemdeservico.core.dto.ListarServicosDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.ServicoRequestJson;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.ServicoResponseJson;
import com.fiap.mecanica.shared.page.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/servico")
@Tag(name = "Serviço")
public class ServicoHttpController {

    private final ServicoCleanController cleanController;

    public ServicoHttpController(ServicoGateway servicoGateway) {
        this.cleanController = new ServicoCleanController(servicoGateway);
    }

    @Operation(summary = "Criar um serviço", description = "Cria um novo serviço oferecido pela mecânica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody @Valid ServicoRequestJson request) {
        cleanController.criar(new CriarServicoDto(request.nome(), request.descricao(), request.preco()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Listar serviços", description = "Retorna a lista paginada de serviços cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<PageResponse<ServicoResponseJson>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PageResponse.from(cleanController.listar(new ListarServicosDto(page, size))));
    }

    @Operation(summary = "Atualizar um serviço", description = "Atualiza os dados de um serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable Long id, @RequestBody @Valid ServicoRequestJson request) {
        cleanController.atualizar(new AtualizarServicoDto(id, request.nome(), request.descricao(), request.preco()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deletar um serviço", description = "Remove um serviço pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cleanController.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
