package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Dados para abertura de uma Ordem de Serviço")
public record OrdemDeServicoRequestJson(
        @Schema(description = "ID do cliente", example = "1")
        @NotNull(message = "O cliente deve ser informado")
        Long clienteId,

        @Schema(description = "ID do veículo", example = "1")
        @NotNull(message = "O veículo deve ser informado")
        Long veiculoId,

        @Schema(description = "IDs dos serviços a vincular (opcional)", example = "[1,2]")
        List<Long> servicosIds,

        @Schema(description = "Peças a vincular com suas quantidades (opcional)")
        @Valid
        List<PecaVinculadaCriarRequestJson> pecas,

        @Schema(description = "Insumos a vincular com suas quantidades (opcional)")
        @Valid
        List<InsumoVinculadoCriarRequestJson> insumos,

        @Schema(description = "Relato do cliente sobre o problema", example = "Barulho ao frear")
        @NotBlank(message = "A descrição deve ser informada")
        @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres")
        String descricao
) {
}
