package com.fiap.mecanica.shared.seguranca.core.domain.password;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PasswordUnitTest {

    @Test
    void shouldReturnValueAfterCreation() {
        Password password = new Password("Senha@123");

        assertEquals("Senha@123", password.getValue());
    }

    @Test
    void shouldExtendPasswordBase() {
        Password password = new Password("Senha@123");

        assertInstanceOf(PasswordBase.class, password);
    }
}
