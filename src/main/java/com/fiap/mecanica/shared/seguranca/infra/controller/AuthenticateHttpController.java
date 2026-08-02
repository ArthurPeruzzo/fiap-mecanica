package com.fiap.mecanica.shared.seguranca.infra.controller;

import com.fiap.mecanica.shared.seguranca.core.gateway.AutenticacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.infra.controller.json.request.LoginRequestJson;
import com.fiap.mecanica.shared.seguranca.infra.controller.json.response.LoginResponseJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/authenticate")
@Tag(name = "Autenticação")
public class AuthenticateHttpController {

    private final AuthenticateCleanController cleanController;

    public AuthenticateHttpController(AutenticacaoGateway autenticacaoGateway, TokenGateway tokenGateway) {
        this.cleanController = new AuthenticateCleanController(autenticacaoGateway, tokenGateway);
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
}
