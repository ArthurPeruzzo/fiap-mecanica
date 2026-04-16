package com.fiap.mecanica.shared.seguranca.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailUnitTest {

    @Test
    void shouldCreateEmailWithValidValue() {
        Email email = new Email("user@example.com");

        assertEquals("user@example.com", email.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@domain.com",
            "user@domain.com.br",
            "user.name@domain.org",
            "user+tag@domain.io"
    })
    void shouldAcceptValidEmailFormats(String validEmail) {
        assertDoesNotThrow(() -> new Email(validEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalidemail",
            "missing-at-sign",
            "@nodomain",
            ""
    })
    void shouldThrowIllegalArgumentExceptionForInvalidEmailFormats(String invalidEmail) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Email(invalidEmail)
        );

        assertEquals(
                "O formato do email não é válido. Deve ser seguido o seguinte formato: exemplo@exemplo.com",
                exception.getMessage()
        );
    }
}
