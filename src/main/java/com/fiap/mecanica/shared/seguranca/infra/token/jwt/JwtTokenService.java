package com.fiap.mecanica.shared.seguranca.infra.token.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.infra.token.dto.TokenParams;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenService implements TokenGateway {

    private static final String EMAIL = "email";
    private static final String ROLES = "roles";
    private static final String AUTHORIZATION = "Authorization";

    private final Algorithm algorithm;
    private final String jwtIssuer;
    private final HttpServletRequest httpServletRequest;

    private String getAuthorization() {
        return httpServletRequest.getHeader(AUTHORIZATION).replace("Bearer ", "");
    }

    @Override
    public String generateToken(TokenParams params) {
        try {
            String userId = String.valueOf(params.userId());
            String token = JWT.create()
                    .withIssuer(jwtIssuer)
                    .withIssuedAt(creationDate())
                    .withExpiresAt(expirationDate())
                    .withSubject(userId)
                    .withClaim(EMAIL, params.email())
                    .withArrayClaim(ROLES, params.roles().toArray(new String[0]))
                    .sign(algorithm);
            return "Bearer " + token;
        } catch (JWTCreationException exception){
            throw new JWTCreationException("Erro ao gerar token.", exception);
        }
    }

    @Override
    public String getEmail() {
        try {
            return decodeToken(getAuthorization()).getClaim(EMAIL).asString();
        } catch (JWTVerificationException ex) {
            log.warn("Falha ao verificar JWT: {}", ex.getMessage());
            throw new JWTVerificationException("Token inválido ou expirado.", ex);
        }
    }

    @Override
    public Long getUserId() {
        try {
            return Long.parseLong(decodeToken(getAuthorization()).getSubject());
        } catch (JWTVerificationException ex) {
            log.warn("Falha ao verificar JWT: {}", ex.getMessage());
            throw new JWTVerificationException("Token inválido ou expirado.", ex);
        }
    }

    @Override
    public List<RoleEnum> getRoles() {
        try {
            return decodeToken(getAuthorization()).getClaim(ROLES).asList(RoleEnum.class);
        } catch (JWTVerificationException ex) {
            log.warn("Falha ao verificar JWT: {}", ex.getMessage());
            throw new JWTVerificationException("Token inválido ou expirado.", ex);
        }
    }

    public DecodedJWT decodeToken(String token) {
        try {
            return JWT.require(algorithm)
                    .withIssuer(jwtIssuer)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException ex) {
            log.warn("Falha ao verificar JWT: {}", ex.getMessage());
            throw new JWTVerificationException("Token inválido ou expirado.", ex);
        }
    }

    private Instant creationDate() {
        return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant();
    }

    private Instant expirationDate() {
        return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
                .plusHours(4)
                .toInstant();
    }

}
