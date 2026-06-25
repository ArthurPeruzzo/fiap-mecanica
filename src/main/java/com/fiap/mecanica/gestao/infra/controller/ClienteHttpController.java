package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.dto.AtualizarClienteDto;
import com.fiap.mecanica.gestao.core.dto.CriarClienteDto;
import com.fiap.mecanica.gestao.core.dto.ListarClientesDto;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.infra.controller.json.ClienteRequestJson;
import com.fiap.mecanica.gestao.infra.controller.json.ClienteResponseJson;
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
@RequestMapping(value = "/cliente")
@Tag(name = "Cliente")
public class ClienteHttpController {

    private final ClienteCleanController cleanController;

    public ClienteHttpController(ClienteGateway clienteGateway) {
        this.cleanController = new ClienteCleanController(clienteGateway);
    }

    @Operation(summary = "Criar um cliente", description = "Cria um novo cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "409", description = "Ja existe um Cliente criado com o documento informado"),
            @ApiResponse(responseCode = "400", description = "Parametros de entrada invalidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<HttpStatus> criar(@RequestBody @Valid ClienteRequestJson request) {
        cleanController.criar(new CriarClienteDto(request.nome(), request.cpf(), request.cnpj()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Listar clientes", description = "Retorna a lista paginada de clientes cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<PageResponse<ClienteResponseJson>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PageResponse.from(cleanController.listar(new ListarClientesDto(page, size))));
    }

    @Operation(summary = "Atualizar um cliente", description = "Atualiza os dados de um cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametros de entrada invalidos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Ja existe um cliente com o documento informado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable Long id, @RequestBody @Valid ClienteRequestJson request) {
        cleanController.atualizar(new AtualizarClienteDto(id, request.nome(), request.cnpj(), request.cpf()));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Deletar um cliente",
            description = "Remove um cliente pelo ID. Ao deletar o cliente os veículos relacionados também são deletados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cleanController.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
