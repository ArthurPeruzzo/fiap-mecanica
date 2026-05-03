package com.fiap.mecanica.shared.seguranca.infra.controller.json.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "LoginRequestJson",
        description = "Dados necessários para fazer o login"
)
public record LoginRequestJson(
        @Schema(
                description = "Email",
                example = "administrador@mecanica.com"
        )
        @NotBlank(message = "O email deve ser preenchido")
        String email,

        @Schema(
                description = "A senha deve conter letras maiúsculas, minúsculas, números e caracteres especiais",
                example = "MeCanica2026!@#"
        )
        @NotBlank(message = "A senha deve ser preenchida")
        String password) {}
