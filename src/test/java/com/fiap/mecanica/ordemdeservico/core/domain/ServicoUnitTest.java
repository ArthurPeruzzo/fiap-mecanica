package com.fiap.mecanica.ordemdeservico.core.domain;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServicoUnitTest {

    private static final String NOME = "Troca de óleo";
    private static final String DESCRICAO = "Troca de óleo do motor com filtro incluso";
    private static final BigDecimal PRECO = new BigDecimal("150.00");

    @Test
    void shouldCreateServicoWithCorrectFields() {
        var servico = new Servico(NOME, DESCRICAO, PRECO);

        assertEquals(NOME, servico.getNome());
        assertEquals(DESCRICAO, servico.getDescricao());
        assertEquals(PRECO, servico.getPreco());
        assertNull(servico.getId());
    }

    @Test
    void reconstituir_shouldSetIdAndPreserveFields() {
        var servico = Servico.reconstituir(1L, NOME, DESCRICAO, PRECO);

        assertEquals(1L, servico.getId());
        assertEquals(NOME, servico.getNome());
        assertEquals(DESCRICAO, servico.getDescricao());
        assertEquals(PRECO, servico.getPreco());
    }

    @Test
    void atualizar_shouldChangeAllFields() {
        var servico = new Servico(NOME, DESCRICAO, PRECO);

        servico.atualizar("Alinhamento", "Alinhamento e balanceamento", new BigDecimal("200.00"));

        assertEquals("Alinhamento", servico.getNome());
        assertEquals("Alinhamento e balanceamento", servico.getDescricao());
        assertEquals(new BigDecimal("200.00"), servico.getPreco());
    }

    @Test
    void reconstituir_shouldNotChangeIdOnSubsequentAtualizar() {
        var servico = Servico.reconstituir(5L, NOME, DESCRICAO, PRECO);

        servico.atualizar("Novo nome", "Nova desc", new BigDecimal("99.00"));

        assertEquals(5L, servico.getId());
        assertEquals("Novo nome", servico.getNome());
    }
}
