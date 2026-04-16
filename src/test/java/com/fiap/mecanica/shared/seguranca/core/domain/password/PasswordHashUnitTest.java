package com.fiap.mecanica.shared.seguranca.core.domain.password;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashUnitTest {

    @Test
    void shouldCreatePasswordHashWithAnyValue() {
        assertDoesNotThrow(() -> new PasswordHash("$2a$10$someHashedValue"));
    }

    @Test
    void shouldReturnStoredValue() {
        String hash = "$2a$10$someHashedValue";
        PasswordHash passwordHash = new PasswordHash(hash);

        assertEquals(hash, passwordHash.getValue());
    }

    @Test
    void shouldAcceptArbitraryStringWithoutValidation() {
        assertDoesNotThrow(() -> new PasswordHash("not-a-real-hash"));
        assertDoesNotThrow(() -> new PasswordHash(""));
        assertDoesNotThrow(() -> new PasswordHash("12345"));
    }

    @Test
    void shouldExtendPasswordBase() {
        PasswordHash passwordHash = new PasswordHash("any-hash");

        assertInstanceOf(PasswordBase.class, passwordHash);
    }
}
