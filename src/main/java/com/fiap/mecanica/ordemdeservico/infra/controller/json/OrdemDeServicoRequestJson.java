package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para abertura de uma Ordem de Serviço")
public record OrdemDeServicoRequestJson(
        @Schema(description = "ID do cliente", example = "1")
        @NotNull(message = "O cliente deve ser informado")
        Long clienteId,

        @Schema(description = "ID do veículo", example = "1")
        @NotNull(message = "O veículo deve ser informado")
        Long veiculoId
) {
}
