package com.fiap.mecanica.shared.seguranca.infra.controller;

import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.AutenticacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.seguranca.infra.controller.json.request.LoginRequestJson;
import com.fiap.mecanica.shared.seguranca.infra.controller.json.response.ClienteStatusResponseJson;
import com.fiap.mecanica.shared.seguranca.infra.controller.json.response.LoginResponseJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/authenticate")
@Tag(name = "Autenticação")
public class AuthenticateHttpController {

    private final AuthenticateCleanController cleanController;

    public AuthenticateHttpController(AutenticacaoGateway autenticacaoGateway,
                                       TokenGateway tokenGateway,
                                       ClienteGateway clienteGateway,
                                       UserGateway userGateway) {
        this.cleanController = new AuthenticateCleanController(autenticacaoGateway, tokenGateway, clienteGateway, userGateway);
    }

    @Operation(
            summary = "Fazer login no sistema",
            description = "Login do sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login executado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Alguma informação que compõe o processo de login não foi encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseJson> login(@RequestBody @Valid LoginRequestJson loginRequestJson) {
        String token = cleanController.login(loginRequestJson.cpf(), loginRequestJson.password());
        return ResponseEntity.ok(new LoginResponseJson(token));
    }

    @Operation(
            summary = "[USO INTERNO] Consultar existência de cliente e resolver identidade",
            description = "Endpoint de uso interno, chamado pela Function Lambda responsável pela autenticação de clientes. " +
                    "Confirma que o CPF informado pertence a um cliente cadastrado e devolve o id do User " +
                    "vinculado a ele — criando esse User na primeira chamada, caso ainda não exista, com ROLE_CLIENTE. " +
                    "A Lambda usa esse id como claim 'sub' do JWT que ela mesma assina; esta aplicação não gera token aqui."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado, userId devolvido"),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente encontrado para o CPF informado")
    })
    @GetMapping("/cliente/status")
    public ResponseEntity<ClienteStatusResponseJson> consultarCliente(@RequestParam String cpf) {
        Long userId = cleanController.consultarCliente(cpf);
        return ResponseEntity.ok(new ClienteStatusResponseJson(userId));
    }
}
