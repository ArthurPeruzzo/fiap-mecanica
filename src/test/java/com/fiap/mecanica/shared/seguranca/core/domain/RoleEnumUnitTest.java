package com.fiap.mecanica.shared.seguranca.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleEnumUnitTest {

    @Test
    void shouldContainRoleAtendente() {
        assertDoesNotThrow(() -> RoleEnum.valueOf("ROLE_ATENDENTE"));
    }

    @Test
    void shouldContainRoleMecanico() {
        assertDoesNotThrow(() -> RoleEnum.valueOf("ROLE_MECANICO"));
    }

    @Test
    void shouldContainRoleAdministrador() {
        assertDoesNotThrow(() -> RoleEnum.valueOf("ROLE_ADMINISTRADOR"));
    }

    @Test
    void shouldHaveExactlyTwoValues() {
        assertEquals(3, RoleEnum.values().length);
    }

    @Test
    void shouldReturnCorrectNameForAtendente() {
        assertEquals("ROLE_ATENDENTE", RoleEnum.ROLE_ATENDENTE.name());
    }

    @Test
    void shouldReturnCorrectNameForMecanico() {
        assertEquals("ROLE_MECANICO", RoleEnum.ROLE_MECANICO.name());
    }

    @Test
    void shouldReturnCorrectNameForAdministrador() {
        assertEquals("ROLE_ADMINISTRADOR", RoleEnum.ROLE_ADMINISTRADOR.name());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForUnknownRole() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RoleEnum.valueOf("ROLE_DESCONHECIDA")
        );
    }
}
