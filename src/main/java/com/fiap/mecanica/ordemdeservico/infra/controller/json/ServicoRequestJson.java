package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(name = "ServicoRequestJson", description = "Dados necessários para criar ou atualizar um serviço")
public record ServicoRequestJson(

        @Schema(description = "Nome do serviço", example = "Troca de óleo")
        @NotBlank(message = "O nome deve ser preenchido")
        String nome,

        @Schema(description = "Descrição do serviço", example = "Troca de óleo do motor com filtro incluso")
        @NotBlank(message = "A descrição deve ser preenchida")
        String descricao,

        @Schema(description = "Preço do serviço", example = "150.00")
        @NotNull(message = "O preço deve ser preenchido")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        BigDecimal preco
) {
}
