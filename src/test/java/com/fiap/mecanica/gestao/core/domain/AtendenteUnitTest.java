package com.fiap.mecanica.gestao.core.domain;

import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtendenteUnitTest {

    @Test
    void shouldBuildAtendenteWithAllFields() {
        var atendente = Atendente.builder()
                .id(1L)
                .nomeCompleto(new NomeCompleto("Ana", "Costa"))
                .build();

        assertEquals(1L, atendente.getId());
        assertEquals("Ana", atendente.getNomeCompleto().nome());
        assertEquals("Costa", atendente.getNomeCompleto().sobrenome());
    }

    @Test
    void shouldBuildAtendenteWithoutId() {
        var atendente = Atendente.builder()
                .nomeCompleto(new NomeCompleto("Ana", "Costa"))
                .build();

        assertNull(atendente.getId());
        assertNotNull(atendente.getNomeCompleto());
    }
}
