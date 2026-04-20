package com.fiap.mecanica.gestao.core.domain;

import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MecanicoUnitTest {

    @Test
    void shouldBuildMecanicoWithAllFields() {
        var mecanico = Mecanico.builder()
                .id(1L)
                .nomeCompleto(new NomeCompleto("Carlos", "Souza"))
                .especialidade("Motor")
                .build();

        assertEquals(1L, mecanico.getId());
        assertEquals("Carlos", mecanico.getNomeCompleto().nome());
        assertEquals("Souza", mecanico.getNomeCompleto().sobrenome());
        assertEquals("Motor", mecanico.getEspecialidade());
    }

    @Test
    void shouldBuildMecanicoWithoutId() {
        var mecanico = Mecanico.builder()
                .nomeCompleto(new NomeCompleto("Carlos", "Souza"))
                .especialidade("Suspensão")
                .build();

        assertNull(mecanico.getId());
        assertNotNull(mecanico.getNomeCompleto());
        assertEquals("Suspensão", mecanico.getEspecialidade());
    }

    @Test
    void shouldBuildMecanicoWithNullEspecialidade() {
        var mecanico = Mecanico.builder()
                .nomeCompleto(new NomeCompleto("Carlos", "Souza"))
                .build();

        assertNull(mecanico.getEspecialidade());
    }
}
