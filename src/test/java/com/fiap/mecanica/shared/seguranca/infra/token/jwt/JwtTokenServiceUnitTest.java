package com.fiap.mecanica.shared.seguranca.infra.token.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.infra.token.dto.TokenParams;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceUnitTest {

    private static final String SECRET = Base64.getUrlEncoder().encodeToString(
            "supersecretkey1234567890123456ab".getBytes()
    );
    private static final String ISSUER = "test-issuer";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(
            Base64.getUrlDecoder().decode(SECRET)
    );

    @Mock
    private HttpServletRequest httpServletRequest;

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(ALGORITHM, ISSUER, httpServletRequest);
    }

    // -------------------------------------------------------------------------
    // generateToken
    // -------------------------------------------------------------------------

    @Test
    void generateToken_shouldReturnBearerPrefixedToken() {
        TokenParams params = new TokenParams(1L, "user@test.com", List.of("ROLE_ATENDENTE"));

        String token = jwtTokenService.generateToken(params);

        assertTrue(token.startsWith("Bearer "));
    }

    @Test
    void generateToken_shouldProduceTokenWithCorrectClaims() {
        TokenParams params = new TokenParams(1L, "user@test.com", List.of("ROLE_ATENDENTE"));

        String token = jwtTokenService.generateToken(params);
        String rawToken = token.replace("Bearer ", "");
        DecodedJWT decoded = jwtTokenService.decodeToken(rawToken);

        assertEquals("1", decoded.getSubject());
        assertEquals("user@test.com", decoded.getClaim("email").asString());
        assertEquals(List.of("ROLE_ATENDENTE"), decoded.getClaim("roles").asList(String.class));
        assertEquals(ISSUER, decoded.getIssuer());
    }

    // -------------------------------------------------------------------------
    // getEmail
    // -------------------------------------------------------------------------

    @Test
    void getEmail_shouldReturnEmailFromValidToken() {
        String rawToken = generateRawToken(1L, "user@test.com", List.of("ROLE_ATENDENTE"));
        Mockito.when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + rawToken);

        String email = jwtTokenService.getEmail();

        assertEquals("user@test.com", email);
    }

    @Test
    void getEmail_shouldThrowJWTVerificationExceptionForInvalidToken() {
        Mockito.when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");

        assertThrows(JWTVerificationException.class, () -> jwtTokenService.getEmail());
    }

    // -------------------------------------------------------------------------
    // getUserId
    // -------------------------------------------------------------------------

    @Test
    void getUserId_shouldReturnUserIdFromValidToken() {
        String rawToken = generateRawToken(42L, "user@test.com", List.of("ROLE_MECANICO"));
        Mockito.when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + rawToken);

        Long userId = jwtTokenService.getUserId();

        assertEquals(42L, userId);
    }

    @Test
    void getUserId_shouldThrowJWTVerificationExceptionForInvalidToken() {
        Mockito.when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");

        assertThrows(JWTVerificationException.class, () -> jwtTokenService.getUserId());
    }

    // -------------------------------------------------------------------------
    // getRoles
    // -------------------------------------------------------------------------

    @Test
    void getRoles_shouldReturnRolesFromValidToken() {
        String rawToken = generateRawToken(1L, "user@test.com", List.of("ROLE_ATENDENTE", "ROLE_MECANICO"));
        Mockito.when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + rawToken);

        List<RoleEnum> roles = jwtTokenService.getRoles();

        assertEquals(2, roles.size());
        assertTrue(roles.contains(RoleEnum.ROLE_ATENDENTE));
        assertTrue(roles.contains(RoleEnum.ROLE_MECANICO));
    }

    @Test
    void getRoles_shouldThrowJWTVerificationExceptionForInvalidToken() {
        Mockito.when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");

        assertThrows(JWTVerificationException.class, () -> jwtTokenService.getRoles());
    }

    // -------------------------------------------------------------------------
    // decodeToken
    // -------------------------------------------------------------------------

    @Test
    void decodeToken_shouldReturnDecodedJWTForValidToken() {
        String rawToken = generateRawToken(1L, "user@test.com", List.of("ROLE_ATENDENTE"));

        DecodedJWT decoded = jwtTokenService.decodeToken(rawToken);

        assertNotNull(decoded);
        assertEquals("1", decoded.getSubject());
    }

    @Test
    void decodeToken_shouldThrowJWTVerificationExceptionForInvalidToken() {
        assertThrows(JWTVerificationException.class,
                () -> jwtTokenService.decodeToken("invalid.token.here"));
    }

    @Test
    void decodeToken_shouldThrowJWTVerificationExceptionForTokenSignedWithDifferentKey() {
        Algorithm otherAlgorithm = Algorithm.HMAC256(
                Base64.getUrlDecoder().decode(
                        Base64.getUrlEncoder().encodeToString("anotherkey1234567890123456789012".getBytes())
                )
        );
        JwtTokenService otherService = new JwtTokenService(otherAlgorithm, ISSUER, httpServletRequest);
        TokenParams params = new TokenParams(1L, "user@test.com", List.of("ROLE_ATENDENTE"));
        String tokenFromOtherKey = otherService.generateToken(params).replace("Bearer ", "");

        assertThrows(JWTVerificationException.class,
                () -> jwtTokenService.decodeToken(tokenFromOtherKey));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private String generateRawToken(Long userId, String email, List<String> roles) {
        TokenParams params = new TokenParams(userId, email, roles);
        return jwtTokenService.generateToken(params).replace("Bearer ", "");
    }
}
