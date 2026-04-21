package com.fiap.mecanica.estoque.core.domain;

import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InsumoUnitTest {

    private static final String NOME = "Óleo de motor";
    private static final String DESCRICAO = "Óleo lubrificante 5W30";
    private static final BigDecimal PRECO = new BigDecimal("45.90");
    private static final UnidadeMedida UNIDADE = UnidadeMedida.LITRO;

    @Test
    void shouldCreateInsumoWithCorrectFields() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 10);

        assertEquals(NOME, insumo.getNome());
        assertEquals(DESCRICAO, insumo.getDescricao());
        assertEquals(PRECO, insumo.getPreco());
        assertEquals(UNIDADE, insumo.getUnidadeMedida());
        assertEquals(10, insumo.getEstoqueTotal());
        assertNull(insumo.getId());
    }

    @Test
    void reconstituir_shouldSetIdAndPreserveFields() {
        var insumo = Insumo.reconstituir(1L, NOME, DESCRICAO, PRECO, UNIDADE, 5);

        assertEquals(1L, insumo.getId());
        assertEquals(NOME, insumo.getNome());
        assertEquals(DESCRICAO, insumo.getDescricao());
        assertEquals(PRECO, insumo.getPreco());
        assertEquals(UNIDADE, insumo.getUnidadeMedida());
        assertEquals(5, insumo.getEstoqueTotal());
    }

    @Test
    void reconstituir_shouldWorkWithDifferentUnidadeMedida() {
        var insumo = Insumo.reconstituir(2L, NOME, DESCRICAO, PRECO, UnidadeMedida.ML, 500);

        assertEquals(UnidadeMedida.ML, insumo.getUnidadeMedida());
        assertEquals(500, insumo.getEstoqueTotal());
    }

    @Test
    void baixarEstoque_shouldDecrementQuantidade() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 10);

        insumo.baixarEstoque(4);

        assertEquals(6, insumo.getEstoqueTotal());
    }

    @Test
    void baixarEstoque_shouldAllowDrainToZero() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 3);

        insumo.baixarEstoque(3);

        assertEquals(0, insumo.getEstoqueTotal());
    }

    @Test
    void baixarEstoque_shouldThrowEstoqueInsuficienteWhenQuantidadeExceedsEstoque() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 2);

        assertThrows(EstoqueInsuficienteException.class, () -> insumo.baixarEstoque(3));
    }

    @Test
    void baixarEstoque_shouldThrowEstoqueInsuficienteWhenEstoqueIsZero() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 0);

        assertThrows(EstoqueInsuficienteException.class, () -> insumo.baixarEstoque(1));
    }

    @Test
    void devolverEstoque_shouldIncrementQuantidade() {
        var insumo = new Insumo(NOME, DESCRICAO, PRECO, UNIDADE, 5);

        insumo.devolverEstoque(3);

        assertEquals(8, insumo.getEstoqueTotal());
    }
}
