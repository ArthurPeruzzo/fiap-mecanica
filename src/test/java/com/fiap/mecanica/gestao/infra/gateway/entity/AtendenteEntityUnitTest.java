package com.fiap.mecanica.gestao.infra.gateway.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtendenteEntityUnitTest {

    @Test
    void shouldCreateWithNoArgsConstructor() {
        assertNotNull(new AtendenteEntity());
    }

    @Test
    void shouldCreateWithAllArgsConstructor() {
        var entity = new AtendenteEntity(1L, "Ana", "Costa", 10L);

        assertEquals(1L, entity.getId());
        assertEquals("Ana", entity.getNome());
        assertEquals("Costa", entity.getSobrenome());
        assertEquals(10L, entity.getUserId());
    }

    @Test
    void shouldBuildWithAllFields() {
        var entity = AtendenteEntity.builder()
                .id(1L)
                .nome("Ana")
                .sobrenome("Costa")
                .userId(10L)
                .build();

        assertEquals(1L, entity.getId());
        assertEquals("Ana", entity.getNome());
        assertEquals("Costa", entity.getSobrenome());
        assertEquals(10L, entity.getUserId());
    }
}
