package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Peça a vincular na criação da Ordem de Serviço")
public record PecaVinculadaCriarRequestJson(
        @Schema(description = "ID da peça", example = "1")
        @NotNull(message = "O ID da peça deve ser informado")
        Long id,

        @Schema(description = "Quantidade a vincular", example = "2")
        @NotNull(message = "A quantidade deve ser informada")
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1")
        Integer quantidade
) {
}
