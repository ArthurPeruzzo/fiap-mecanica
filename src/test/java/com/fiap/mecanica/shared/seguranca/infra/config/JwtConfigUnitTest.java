package com.fiap.mecanica.shared.seguranca.infra.config;

import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigUnitTest {

    private static final String SECRET = Base64.getUrlEncoder().encodeToString(
            "supersecretkey1234567890123456ab".getBytes()
    );
    private static final String ISSUER = "test-issuer";

    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() throws Exception {
        jwtConfig = new JwtConfig();

        Field secretKeyField = JwtConfig.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtConfig, SECRET);

        Field issuerField = JwtConfig.class.getDeclaredField("issuer");
        issuerField.setAccessible(true);
        issuerField.set(jwtConfig, ISSUER);
    }

    @Test
    void jwtAlgorithm_shouldReturnNonNullAlgorithm() {
        Algorithm algorithm = jwtConfig.jwtAlgorithm();

        assertNotNull(algorithm);
    }

    @Test
    void jwtAlgorithm_shouldThrowWhenSecretIsInvalidBase64() throws Exception {
        Field secretKeyField = JwtConfig.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtConfig, "not-valid-base64!!!");

        assertThrows(Exception.class, () -> jwtConfig.jwtAlgorithm());
    }

    @Test
    void jwtIssuer_shouldReturnConfiguredIssuer() {
        String issuer = jwtConfig.jwtIssuer();

        assertEquals(ISSUER, issuer);
    }
}
