package com.fiap.mecanica.gestao.infra.controller.json;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "VeiculoResponseJson", description = "Dados do veículo")
public record VeiculoResponseJson(

        @Schema(description = "ID do veículo", example = "1")
        Long id,

        @Schema(description = "ID do cliente proprietário", example = "1")
        Long clienteId,

        @Schema(description = "Placa do veículo formatada", example = "ABC-1234")
        String placa,

        @Schema(description = "Modelo do veículo", example = "Gol")
        String modelo,

        @Schema(description = "Ano de fabricação", example = "2020")
        Integer ano
) {
    public static VeiculoResponseJson from(Veiculo veiculo) {
        return new VeiculoResponseJson(
                veiculo.getId(),
                veiculo.getClienteId(),
                veiculo.getPlaca().getValorFormatado(),
                veiculo.getModelo(),
                veiculo.getAno()
        );
    }
}
