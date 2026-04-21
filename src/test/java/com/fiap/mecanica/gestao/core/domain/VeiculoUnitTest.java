package com.fiap.mecanica.gestao.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VeiculoUnitTest {

    @Test
    void shouldCreateVeiculoWithPlacaAntiga() {
        var veiculo = new Veiculo(1L, "ABC1234", "Gol", 2020);

        assertNotNull(veiculo);
        assertEquals("ABC1234", veiculo.getPlaca().getValor());
        assertEquals("Gol", veiculo.getModelo());
        assertEquals(2020, veiculo.getAno());
        assertEquals(1L, veiculo.getClienteId());
    }

    @Test
    void shouldCreateVeiculoWithPlacaMercosul() {
        var veiculo = new Veiculo(1L, "ABC1D23", "Onix", 2023);

        assertEquals("ABC1D23", veiculo.getPlaca().getValor());
    }

    @Test
    void shouldStripHyphenFromPlacaOnCreation() {
        var veiculo = new Veiculo(1L, "ABC-1234", "Gol", 2020);

        assertEquals("ABC1234", veiculo.getPlaca().getValor());
    }

    @Test
    void reconstituir_shouldSetIdAndPreserveFields() {
        var veiculo = Veiculo.reconstituir(42L, 1L, "ABC1D23", "Gol", 2020);

        assertEquals(42L, veiculo.getId());
        assertEquals(1L, veiculo.getClienteId());
        assertEquals("ABC1D23", veiculo.getPlaca().getValor());
        assertEquals("Gol", veiculo.getModelo());
        assertEquals(2020, veiculo.getAno());
    }

    @Test
    void atualizar_shouldChangePlacaModeloAndAno() {
        var veiculo = new Veiculo(1L, "ABC1234", "Gol", 2020);

        veiculo.atualizar("ABC1D23", "Onix", 2023);

        assertEquals("ABC1D23", veiculo.getPlaca().getValor());
        assertEquals("Onix", veiculo.getModelo());
        assertEquals(2023, veiculo.getAno());
    }

    @Test
    void atualizar_shouldStripHyphenFromPlaca() {
        var veiculo = new Veiculo(1L, "ABC1234", "Gol", 2020);

        veiculo.atualizar("ABC-1D23", "Onix", 2023);

        assertEquals("ABC1D23", veiculo.getPlaca().getValor());
    }

    @Test
    void atualizar_shouldNotChangeClienteId() {
        var veiculo = new Veiculo(1L, "ABC1234", "Gol", 2020);

        veiculo.atualizar("ABC1D23", "Onix", 2023);

        assertEquals(1L, veiculo.getClienteId());
    }
}
