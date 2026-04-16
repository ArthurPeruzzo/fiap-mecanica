package com.fiap.mecanica.shared.seguranca.core.domain.password;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUnitTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "Senha@123",
            "Ab1@abcd",
            "MinhaSenh@1",
            "Test#1234"
    })
    void shouldCreatePasswordWithValidValue(String validPassword) {
        assertDoesNotThrow(() -> new Password(validPassword));
    }

    @Test
    void shouldReturnValueAfterCreation() {
        Password password = new Password("Senha@123");

        assertEquals("Senha@123", password.getValue());
    }

    @Test
    void shouldThrowWhenPasswordIsTooShort() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Password("Ab1@abc")
        );
    }

    @Test
    void shouldThrowWhenPasswordIsTooLong() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Password("Ab1@abcdefghijklmnopq")
        );
    }

    @Test
    void shouldThrowWhenPasswordHasNoUppercase() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Password("senha@123")
        );
    }

    @Test
    void shouldThrowWhenPasswordHasNoLowercase() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Password("SENHA@123")
        );
    }

    @Test
    void shouldThrowWhenPasswordHasNoDigit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Password("Senha@abc")
        );
    }

    @Test
    void shouldThrowWhenPasswordHasNoSpecialChar() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Password("Senha1234")
        );
    }

    @Test
    void shouldThrowWhenPasswordContainsSpaces() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Password("Senha @123")
        );
    }

    @Test
    void shouldThrowWhenPasswordIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Password("")
        );
    }

    @Test
    void shouldExtendPasswordBase() {
        Password password = new Password("Senha@123");

        assertInstanceOf(PasswordBase.class, password);
    }
}
