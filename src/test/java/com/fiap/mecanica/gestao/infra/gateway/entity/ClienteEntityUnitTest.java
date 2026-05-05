package com.fiap.mecanica.gestao.infra.gateway.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClienteEntityUnitTest {

    @Test
    void shouldCreateWithAllArgsConstructor() {
        var entity = new ClienteEntity(1L, "Pedro", "12345678900012", null, new ArrayList<>());

        assertEquals(1L, entity.getId());
        assertEquals("Pedro", entity.getNome());
        assertEquals("12345678900012", entity.getCnpj());
        assertEquals(0, entity.getVeiculos().size());
        assertNull(entity.getCpf());
    }

    @Test
    void shouldBuildWithCpfAndNullCnpj() {
        var entity = ClienteEntity.builder()
                .id(1L)
                .nome("Pedro")
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
                .cnpj("00000000000191")
                .build();

        assertEquals("00000000000191", entity.getCnpj());
        assertNull(entity.getCpf());
    }
}
