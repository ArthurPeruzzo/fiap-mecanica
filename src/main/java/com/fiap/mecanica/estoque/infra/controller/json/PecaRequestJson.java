package com.fiap.mecanica.estoque.infra.controller.json;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PecaRequestJson(
        @NotBlank(message = "O nome deve ser preenchido") String nome,
        @NotBlank(message = "A descrição deve ser preenchida") String descricao,
        @NotNull(message = "O preço deve ser preenchido")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero") BigDecimal preco,
        @NotNull(message = "A quantidade em estoque deve ser preenchida")
        @Min(value = 0, message = "A quantidade em estoque não pode ser negativa") Integer quantidadeEstoque
) {
}
