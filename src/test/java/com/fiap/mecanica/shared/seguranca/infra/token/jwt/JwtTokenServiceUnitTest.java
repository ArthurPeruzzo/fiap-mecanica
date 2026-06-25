package com.fiap.mecanica.shared.seguranca.infra.token.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fiap.mecanica.shared.seguranca.core.domain.Email;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
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

    private User buildUser(Long id, String email, RoleEnum... roleEnums) {
        List<Role> roles = java.util.Arrays.stream(roleEnums)
                .map(r -> new Role(1L, r))
                .toList();
        return new User(id, new Email(email), new PasswordHash("hash"), roles);
    }

    private String generateRawToken(User user) {
        return jwtTokenService.generateToken(user).replace("Bearer ", "");
    }

    // -------------------------------------------------------------------------
    // generateToken
    // -------------------------------------------------------------------------

    @Test
    void generateToken_shouldReturnBearerPrefixedToken() {
        var user = buildUser(1L, "user@test.com", RoleEnum.ROLE_ATENDENTE);

        String token = jwtTokenService.generateToken(user);

        assertTrue(token.startsWith("Bearer "));
    }

    @Test
    void generateToken_shouldProduceTokenWithCorrectClaims() {
        var user = buildUser(1L, "user@test.com", RoleEnum.ROLE_ATENDENTE);

        String token = jwtTokenService.generateToken(user);
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
        var user = buildUser(1L, "user@test.com", RoleEnum.ROLE_ATENDENTE);
        String rawToken = generateRawToken(user);
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
        var user = buildUser(42L, "user@test.com", RoleEnum.ROLE_MECANICO);
        String rawToken = generateRawToken(user);
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
        var user = buildUser(1L, "user@test.com", RoleEnum.ROLE_ATENDENTE, RoleEnum.ROLE_MECANICO);
        String rawToken = generateRawToken(user);
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
        var user = buildUser(1L, "user@test.com", RoleEnum.ROLE_ATENDENTE);
        String rawToken = generateRawToken(user);

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
        var user = buildUser(1L, "user@test.com", RoleEnum.ROLE_ATENDENTE);
        String tokenFromOtherKey = otherService.generateToken(user).replace("Bearer ", "");

        assertThrows(JWTVerificationException.class,
                () -> jwtTokenService.decodeToken(tokenFromOtherKey));
    }
}
