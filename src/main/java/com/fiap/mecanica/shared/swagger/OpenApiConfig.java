package com.fiap.mecanica.shared.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        tags = {
                @Tag(name = "Autenticação", description = "Autenticação"),
                @Tag(name = "Cliente", description = "Operações relacionadas ao cliente"),
                @Tag(name = "Veículo", description = "Operações relacionadas ao veículo"),
                @Tag(name = "Peça", description = "Operações relacionadas as peças"),
                @Tag(name = "Insumo", description = "Operações relacionadas aos insumos"),
                @Tag(name = "Serviço", description = "Operações relacionadas aos serviços"),
                @Tag(name = "Ordem de Serviço", description = "Operações relacionadas a ordem de serviços"),
                @Tag(name = "Orçamento", description = "Operações relacionadas ao orçamento da ordem de serviço"),
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}


