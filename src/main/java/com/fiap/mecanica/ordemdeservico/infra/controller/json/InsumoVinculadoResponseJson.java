package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "InsumoVinculadoResponseJson", description = "Insumo vinculado à ordem de serviço")
public record InsumoVinculadoResponseJson(

        @Schema(description = "ID do insumo cadastrado no estoque", example = "1")
        Long insumoId,

        @Schema(description = "Quantidade consumida", example = "4")
        Integer quantidade,

        @Schema(description = "Preço unitário do insumo no momento do diagnóstico", example = "38.00")
        BigDecimal preco,

        @Schema(description = "É a multiplicação do preco pela quantidade do insumo", example = "89.90")
        BigDecimal valorTotal
) {
}
