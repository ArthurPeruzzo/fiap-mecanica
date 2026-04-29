package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para desvincular um insumo à ordem de serviço")
public record DesvincularInsumoRequestJson(
        @Schema(description = "Quantidade de insumos a desvincular", example = "2")
        @NotNull(message = "A quantidade deve ser informada")
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1")
        Integer quantidade
) {
}
