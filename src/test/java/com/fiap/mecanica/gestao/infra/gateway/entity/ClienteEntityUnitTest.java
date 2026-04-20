package com.fiap.mecanica.gestao.infra.gateway.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteEntityUnitTest {

    @Test
    void shouldCreateWithNoArgsConstructor() {
        assertNotNull(new ClienteEntity());
    }

    @Test
    void shouldCreateWithAllArgsConstructor() {
        var entity = new ClienteEntity(1L, "Pedro", "Silva", "12345678900012", null);

        assertEquals(1L, entity.getId());
        assertEquals("Pedro", entity.getNome());
        assertEquals("Silva", entity.getSobrenome());
        assertEquals("12345678900012", entity.getCnpj());
        assertNull(entity.getCpf());
    }

    @Test
    void shouldBuildWithCpfAndNullCnpj() {
        var entity = ClienteEntity.builder()
                .id(1L)
                .nome("Pedro")
                .sobrenome("Silva")
                .cpf("12345678909")
                .build();

        assertEquals("12345678909", entity.getCpf());
        assertNull(entity.getCnpj());
    }

    @Test
    void shouldBuildWithCnpjAndNullCpf() {
        var entity = ClienteEntity.builder()
                .id(2L)
                .nome("Empresa")
                .sobrenome("LTDA")
                .cnpj("00000000000191")
                .build();

        assertEquals("00000000000191", entity.getCnpj());
        assertNull(entity.getCpf());
    }
}
