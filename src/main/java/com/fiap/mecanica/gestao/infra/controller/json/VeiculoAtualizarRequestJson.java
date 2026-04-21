package com.fiap.mecanica.gestao.infra.controller.json;

import com.fiap.mecanica.gestao.infra.controller.validation.PlacaValida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "VeiculoAtualizarRequestJson",
        description = "Dados para atualizar um veículo. O cliente vinculado não pode ser alterado."
)
public record VeiculoAtualizarRequestJson(

        @Schema(
                description = "Placa do veículo. Formatos aceitos: ABC1234 (placa antiga) ou ABC1D23 (Mercosul), com ou sem hífen",
                example = "ABC1D23"
        )
        @NotBlank(message = "A placa deve ser preenchida")
        @PlacaValida
        String placa,

        @Schema(description = "Modelo do veículo", example = "Gol")
        @NotBlank(message = "O modelo deve ser preenchido")
        String modelo,

        @Schema(description = "Ano de fabricação do veículo", example = "2020")
        @NotNull(message = "O ano deve ser preenchido")
        @Min(value = 1886, message = "O ano informado é inválido")
        @Max(value = 2100, message = "O ano informado é inválido")
        Integer ano
) {
}
