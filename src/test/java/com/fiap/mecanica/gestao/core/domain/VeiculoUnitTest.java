package com.fiap.mecanica.gestao.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class VeiculoUnitTest {

    @Test
    void shouldCreateVeiculoWithNoArgsConstructor() {
        assertNotNull(new Veiculo());
    }

    @Test
    void shouldCreateVeiculoWithAllArgsConstructor() {
        assertNotNull(new Veiculo(1L, "ABC1D23", "Gol", "2020"));
    }

    @Test
    void shouldCreateVeiculoWithBuilder() {
        var veiculo = Veiculo.builder()
                .id(1L)
                .placa("ABC1D23")
                .modelo("Gol")
                .ano("2020")
                .build();

        assertNotNull(veiculo);
    }
}
