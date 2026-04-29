package com.fiap.mecanica.estoque.core.domain;

import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PecaUnitTest {

    private static final String NOME = "Filtro de óleo";
    private static final String DESCRICAO = "Filtro de óleo para motor 1.0";
    private static final BigDecimal PRECO = new BigDecimal("29.90");

    @Test
    void shouldCreatePecaWithCorrectFields() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 10);

        assertEquals(NOME, peca.getNome());
        assertEquals(DESCRICAO, peca.getDescricao());
        assertEquals(PRECO, peca.getPreco());
        assertEquals(10, peca.getEstoqueTotal());
        assertNull(peca.getId());
    }

    @Test
    void reconstituir_shouldSetIdAndPreserveFields() {
        var peca = Peca.reconstituir(1L, NOME, DESCRICAO, PRECO, 5);

        assertEquals(1L, peca.getId());
        assertEquals(NOME, peca.getNome());
        assertEquals(DESCRICAO, peca.getDescricao());
        assertEquals(PRECO, peca.getPreco());
        assertEquals(5, peca.getEstoqueTotal());
    }

    @Test
    void baixarEstoque_shouldDecrementQuantidade() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 10);

        peca.baixarEstoque(3);

        assertEquals(7, peca.getEstoqueTotal());
    }

    @Test
    void baixarEstoque_shouldAllowDrainToZero() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 5);

        peca.baixarEstoque(5);

        assertEquals(0, peca.getEstoqueTotal());
    }

    @Test
    void baixarEstoque_shouldThrowEstoqueInsuficienteWhenQuantidadeExceedsEstoque() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 2);

        assertThrows(EstoqueInsuficienteException.class, () -> peca.baixarEstoque(3));
    }

    @Test
    void baixarEstoque_shouldThrowEstoqueInsuficienteWhenEstoqueIsZero() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 0);

        assertThrows(EstoqueInsuficienteException.class, () -> peca.baixarEstoque(1));
    }

    @Test
    void atualizar_shouldChangeAllFields() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 10);

        peca.atualizar("Vela NGK", "Vela de ignição", new BigDecimal("18.00"), 25);

        assertEquals("Vela NGK", peca.getNome());
        assertEquals("Vela de ignição", peca.getDescricao());
        assertEquals(new BigDecimal("18.00"), peca.getPreco());
        assertEquals(25, peca.getEstoqueTotal());
    }

    @Test
    void atualizar_shouldAllowZeroQuantidade() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 10);

        peca.atualizar(NOME, DESCRICAO, PRECO, 0);

        assertEquals(0, peca.getEstoqueTotal());
    }

    @Test
    void devolverEstoque_shouldIncrementQuantidade() {
        var peca = new Peca(NOME, DESCRICAO, PRECO, 5);

        peca.devolverEstoque(3);

        assertEquals(8, peca.getEstoqueTotal());
    }
}
