package com.fiap.mecanica.shared.seguranca.infra.token.servico;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicoTokenGeneratorUnitTest {

    private static final String SEGREDO = "1URzZ8Xs1hUpErHg31CdZxobDP0rO-oJof_KPv91VGeykuP05SWRghLjnMgYQOuI";
    private static final String SEGREDO_DE_USUARIO = "fIACMIxZHpR5rhnyNTLfwJZOgDtZZVweeS1eRWuT6IZ_U5cRrinFFHJAeSHMpvPKHG_KLM-JTelvD5s3ulA";
    private static final String ISSUER = "mecanica-fiap-servicos";
    private static final String SUBJECT = "svc:fiap-mecanica";
    private static final Duration TTL = Duration.ofSeconds(90);

    private final ServicoTokenGenerator generator = new ServicoTokenGenerator(SEGREDO, ISSUER, SUBJECT, TTL);

    private DecodedJWT verificar(String token) {
        return JWT.require(Algorithm.HMAC256(Base64.getUrlDecoder().decode(SEGREDO)))
                .withIssuer(ISSUER)
                .withAudience("fiap-estoque")
                .build()
                .verify(token);
    }

    @Test
    void shouldGerarTokenComIdentidadeAudienciaEEscopo() {
        DecodedJWT jwt = verificar(generator.gerar("fiap-estoque", List.of("reserva:escrita")));

        assertEquals(ISSUER, jwt.getIssuer());
        assertEquals(SUBJECT, jwt.getSubject());
        assertEquals(List.of("fiap-estoque"), jwt.getAudience());
        assertEquals("reserva:escrita", jwt.getClaim("scope").asString());
        assertNotNull(jwt.getId());
    }

    @Test
    void shouldSepararVariosEscoposPorEspaco() {
        DecodedJWT jwt = verificar(generator.gerar("fiap-estoque", List.of("catalogo:leitura", "reserva:escrita")));

        assertEquals("catalogo:leitura reserva:escrita", jwt.getClaim("scope").asString());
    }

    @Test
    void shouldExpirarConformeOTtlConfigurado() {
        DecodedJWT jwt = verificar(generator.gerar("fiap-estoque", List.of("reserva:escrita")));

        long duracao = jwt.getExpiresAtAsInstant().getEpochSecond() - jwt.getIssuedAtAsInstant().getEpochSecond();
        assertEquals(TTL.toSeconds(), duracao);
    }

    @Test
    void shouldGerarTokenDiferenteACadaChamada() {
        String primeiro = generator.gerar("fiap-estoque", List.of("reserva:escrita"));
        String segundo = generator.gerar("fiap-estoque", List.of("reserva:escrita"));

        assertNotEquals(primeiro, segundo);
        assertNotEquals(verificar(primeiro).getId(), verificar(segundo).getId());
    }

    @Test
    void shouldNaoSerValidoComOSegredoDeUsuario() {
        String token = generator.gerar("fiap-estoque", List.of("reserva:escrita"));

        var verificadorDeUsuario = JWT.require(Algorithm.HMAC256(Base64.getUrlDecoder().decode(SEGREDO_DE_USUARIO)))
                .withIssuer(ISSUER)
                .build();

        assertThrows(JWTVerificationException.class, () -> verificadorDeUsuario.verify(token));
    }
}
