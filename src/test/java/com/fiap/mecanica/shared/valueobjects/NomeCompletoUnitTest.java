package com.fiap.mecanica.shared.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NomeCompletoUnitTest {

    @Test
    void shouldStoreNomeAndSobrenome() {
        var nomeCompleto = new NomeCompleto("Pedro", "Silva");

        assertEquals("Pedro", nomeCompleto.nome());
        assertEquals("Silva", nomeCompleto.sobrenome());
    }

    @Test
    void shouldConcatenateNomeCompletoWithSpace() {
        var nomeCompleto = new NomeCompleto("Pedro", "Silva");

        assertEquals("Pedro Silva", nomeCompleto.nomeCompleto());
    }
}
