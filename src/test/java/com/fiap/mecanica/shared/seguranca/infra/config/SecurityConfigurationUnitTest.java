package com.fiap.mecanica.shared.seguranca.infra.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationUnitTest {

    @Mock
    private UserAuthenticationFilter userAuthenticationFilter;

    @InjectMocks
    private SecurityConfiguration securityConfiguration;

    @Test
    void roleConstants_shouldHaveCorrectValues() {
        assertEquals("ATENDENTE", SecurityConfiguration.ATENDENTE);
        assertEquals("MECANICO", SecurityConfiguration.MECANICO);
    }

    @Test
    void endpointsSemAutenticacao_shouldContainExpectedPaths() {
        String[] endpoints = SecurityConfiguration.ENDPOINTS_SEM_AUTENTICACAO;

        assertArrayEquals(new String[]{
                "/authenticate/login",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/favicon.ico"
        }, endpoints);
    }

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfiguration.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void passwordEncoder_shouldEncodeAndMatchPassword() {
        PasswordEncoder encoder = securityConfiguration.passwordEncoder();
        String rawPassword = "Senha@123";

        String encoded = encoder.encode(rawPassword);

        assertTrue(encoder.matches(rawPassword, encoded));
    }

    @Test
    void passwordEncoder_shouldNotMatchWrongPassword() {
        PasswordEncoder encoder = securityConfiguration.passwordEncoder();

        String encoded = encoder.encode("Senha@123");

        assertFalse(encoder.matches("OutraSenha@123", encoded));
    }
}
