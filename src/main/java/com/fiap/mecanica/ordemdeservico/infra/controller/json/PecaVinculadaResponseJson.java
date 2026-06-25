package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "PecaVinculadaResponseJson", description = "Peça vinculada à ordem de serviço")
public record PecaVinculadaResponseJson(

        @Schema(description = "ID da peça cadastrada no estoque", example = "1")
        Long pecaId,

        @Schema(description = "Quantidade consumida", example = "2")
        Integer quantidade,

        @Schema(description = "Preço unitário da peça no momento do diagnóstico", example = "89.90")
        BigDecimal preco,

        @Schema(description = "É a multiplicação do preco pela quantidade da peça", example = "89.90")
        BigDecimal valorTotal
) {
}
