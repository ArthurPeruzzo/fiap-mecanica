package com.fiap.mecanica.shared.seguranca.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailUnitTest {

    @Test
    void shouldCreateEmailWithValidValue() {
        Email email = new Email("user@example.com");

        assertEquals("user@example.com", email.value());
    }
}
