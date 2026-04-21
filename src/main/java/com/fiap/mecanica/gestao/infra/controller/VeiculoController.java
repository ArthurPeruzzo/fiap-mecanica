package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.dto.AtualizarVeiculoDto;
import com.fiap.mecanica.gestao.core.dto.CriarVeiculoDto;
import com.fiap.mecanica.gestao.core.dto.ListarVeiculosDto;
import com.fiap.mecanica.gestao.core.usecase.AtualizarVeiculoUseCase;
import com.fiap.mecanica.gestao.core.usecase.CriarVeiculoUseCase;
import com.fiap.mecanica.gestao.core.usecase.DeletarVeiculoUseCase;
import com.fiap.mecanica.gestao.core.usecase.ListarVeiculosUseCase;
import com.fiap.mecanica.gestao.infra.controller.json.VeiculoAtualizarRequestJson;
import com.fiap.mecanica.gestao.infra.controller.json.VeiculoRequestJson;
import com.fiap.mecanica.gestao.infra.controller.json.VeiculoResponseJson;
import com.fiap.mecanica.shared.page.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping(value = "/veiculo")
@Tag(name = "Veículo")
public class VeiculoController {

    private final CriarVeiculoUseCase criarVeiculoUseCase;
    private final AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    private final DeletarVeiculoUseCase deletarVeiculoUseCase;
    private final ListarVeiculosUseCase listarVeiculosUseCase;

    @Operation(
            summary = "Criar um veiculo",
            description = "Cria um novo veiculo vinculado ao cliente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Veiculo criado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Ja existe um Veiculo criado com a placa informada"),
            @ApiResponse(responseCode = "400", description = "Parametros de entrada invalidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<HttpStatus> criar(@RequestBody @Valid VeiculoRequestJson veiculoRequestJson) {
        var dto = new CriarVeiculoDto(
                veiculoRequestJson.clienteId(),
                veiculoRequestJson.placa(),
                veiculoRequestJson.modelo(),
                veiculoRequestJson.ano()
        );
        criarVeiculoUseCase.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Listar veiculos",
            description = "Retorna a lista paginada de veiculos cadastrados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<PageResponse<VeiculoResponseJson>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pagina = listarVeiculosUseCase.listar(new ListarVeiculosDto(page, size));
        return ResponseEntity.ok(PageResponse.from(pagina.map(VeiculoResponseJson::from)));
    }

    @Operation(
            summary = "Atualizar um veiculo",
            description = "Atualiza placa, modelo e ano de um veículo. O cliente vinculado não pode ser alterado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Veiculo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametros de entrada invalidos"),
            @ApiResponse(responseCode = "404", description = "Veiculo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Ja existe um veiculo com a placa informada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable Long id, @RequestBody @Valid VeiculoAtualizarRequestJson req) {
        var dto = new AtualizarVeiculoDto(id, req.placa(), req.modelo(), req.ano());
        atualizarVeiculoUseCase.atualizar(dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Deletar um veiculo",
            description = "Remove um veículo pelo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Veiculo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Veiculo não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        deletarVeiculoUseCase.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
