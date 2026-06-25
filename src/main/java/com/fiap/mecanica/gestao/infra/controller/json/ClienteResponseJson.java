package com.fiap.mecanica.gestao.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClienteResponseJson", description = "Dados do cliente")
public record ClienteResponseJson(

        @Schema(description = "ID do cliente", example = "1")
        Long id,

        @Schema(description = "Nome do cliente", example = "Pedro")
        String nome,

        @Schema(description = "CPF do cliente (somente dígitos)", example = "18825469039")
        String cpf,

        @Schema(description = "CNPJ do cliente (somente caracteres do documento)", example = "9BX1W34S000144")
        String cnpj
) {}
