package com.fiap.mecanica.gestao.infra.gateway.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MecanicoEntityUnitTest {

    @Test
    void shouldCreateWithNoArgsConstructor() {
        assertNotNull(new MecanicoEntity());
    }

    @Test
    void shouldCreateWithAllArgsConstructor() {
        var entity = new MecanicoEntity(1L, "Carlos", "Souza", "Motor", 20L);

        assertEquals(1L, entity.getId());
        assertEquals("Carlos", entity.getNome());
        assertEquals("Souza", entity.getSobrenome());
        assertEquals("Motor", entity.getEspecialidade());
        assertEquals(20L, entity.getUserId());
    }

    @Test
    void shouldBuildWithAllFields() {
        var entity = MecanicoEntity.builder()
                .id(1L)
                .nome("Carlos")
                .sobrenome("Souza")
                .especialidade("Suspensão")
                .userId(20L)
                .build();

        assertEquals(1L, entity.getId());
        assertEquals("Carlos", entity.getNome());
        assertEquals("Souza", entity.getSobrenome());
        assertEquals("Suspensão", entity.getEspecialidade());
        assertEquals(20L, entity.getUserId());
    }
}
