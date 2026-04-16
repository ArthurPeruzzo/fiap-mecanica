package com.fiap.mecanica.shared.seguranca.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleUnitTest {

    @Test
    void shouldCreateRoleWithIdAndRoleEnum() {
        Role role = new Role(1L, RoleEnum.ROLE_ATENDENTE);

        assertEquals(RoleEnum.ROLE_ATENDENTE, role.getName());
    }

    @Test
    void shouldCreateRoleWithRoleEnumOnly() {
        Role role = new Role(RoleEnum.ROLE_MECANICO);

        assertEquals(RoleEnum.ROLE_MECANICO, role.getName());
    }

    @Test
    void shouldCreateRoleWithNoArgsConstructor() {
        Role role = new Role();

        assertNull(role.getName());
    }

    @Test
    void shouldReturnCorrectNameForAtendente() {
        Role role = new Role(RoleEnum.ROLE_ATENDENTE);

        assertEquals(RoleEnum.ROLE_ATENDENTE, role.getName());
    }
}
