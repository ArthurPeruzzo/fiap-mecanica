package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fiap.mecanica.shared.seguranca.infra.token.servico.ServicoTokenGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class EstoqueTokenInterceptorUnitTest {

    private static final String SEGREDO = "1URzZ8Xs1hUpErHg31CdZxobDP0rO-oJof_KPv91VGeykuP05SWRghLjnMgYQOuI";
    private static final String ISSUER = "mecanica-fiap-servicos";

    private final EstoqueTokenInterceptor interceptor = new EstoqueTokenInterceptor(
            new ServicoTokenGenerator(SEGREDO, ISSUER, "svc:fiap-mecanica", Duration.ofSeconds(90)));

    private final ClientHttpRequestExecution execution = Mockito.mock(ClientHttpRequestExecution.class);

    private MockClientHttpRequest chamar(HttpMethod metodo, String uri) throws IOException {
        var request = new MockClientHttpRequest(metodo, URI.create(uri));
        interceptor.intercept(request, new byte[0], execution);
        return request;
    }

    private DecodedJWT decodificar(MockClientHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        assertNotNull(authorization);
        assertTrue(authorization.startsWith("Bearer "));
        return JWT.require(Algorithm.HMAC256(Base64.getUrlDecoder().decode(SEGREDO)))
                .withIssuer(ISSUER)
                .withAudience("fiap-estoque")
                .build()
                .verify(authorization.substring("Bearer ".length()));
    }

    @Test
    void shouldUsarEscopoDeLeituraNoCatalogo() throws IOException {
        var request = chamar(HttpMethod.GET, "http://estoque:8081/catalogo/pecas?ids=1,2");

        assertEquals("catalogo:leitura", decodificar(request).getClaim("scope").asString());
        Mockito.verify(execution).execute(Mockito.eq(request), Mockito.any());
    }

    @Test
    void shouldUsarEscopoDeEscritaNaReserva() throws IOException {
        var request = chamar(HttpMethod.PUT, "http://estoque:8081/reserva/ordem-servico/7/itens");

        assertEquals("reserva:escrita", decodificar(request).getClaim("scope").asString());
    }

    @Test
    void shouldNaoEnviarTokenParaRotaDesconhecida() throws IOException {
        var request = chamar(HttpMethod.GET, "http://estoque:8081/peca");

        assertNull(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        Mockito.verify(execution).execute(Mockito.eq(request), Mockito.any());
    }

    @Test
    void shouldGerarUmTokenNovoACadaChamada() throws IOException {
        var primeira = chamar(HttpMethod.POST, "http://estoque:8081/reserva/ordem-servico/7/confirmar");
        var segunda = chamar(HttpMethod.POST, "http://estoque:8081/reserva/ordem-servico/7/confirmar");

        assertNotEquals(decodificar(primeira).getId(), decodificar(segunda).getId());
    }
}
