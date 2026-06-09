package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Insumo a vincular na criação da Ordem de Serviço")
public record InsumoVinculadoCriarRequestJson(
        @Schema(description = "ID do insumo", example = "1")
        @NotNull(message = "O ID do insumo deve ser informado")
        Long id,

        @Schema(description = "Quantidade a vincular", example = "3")
        @NotNull(message = "A quantidade deve ser informada")
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1")
        Integer quantidade
) {
}
