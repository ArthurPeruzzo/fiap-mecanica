package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para desvincular uma peça à ordem de serviço")
public record DesvincularPecaRequestJson(
        @Schema(description = "Quantidade de peças a desvincular", example = "2")
        @NotNull(message = "A quantidade deve ser informada")
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1")
        Integer quantidade
) {
}
