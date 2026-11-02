package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

/**
 * As chamadas ao fiap-estoque saem com o cabeçalho W3C {@code traceparent}, para o trace da requisição continuar
 * no outro serviço. O RestClient precisa ser construído com o ObservationRegistry do Spring: o
 * {@code RestClient.builder()} puro não é instrumentado e o contexto de trace não sai do monólito.
 */
@TestPropertySource(properties = "management.tracing.sampling.probability=1.0")
class PropagacaoDeTraceEstoqueIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private static final Pattern TRACEPARENT = Pattern.compile("00-([0-9a-f]{32})-([0-9a-f]{16})-01");

    @Test
    void shouldEnviarOTraceparentNasChamadasAoEstoqueComOMesmoTraceDaRequisicao() {
        estoque.stubFor(put(urlPathMatching(RESERVAR_ITENS)).willReturn(aResponse().withStatus(204)));
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        stubCatalogo();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + obterTokenAtendente())
                .body(String.format("""
                        {"clienteId":%d,"veiculoId":%d,"descricao":"Barulho ao frear",
                         "pecas":[{"id":%d,"quantidade":2}],"insumos":[{"id":%d,"quantidade":3}]}""",
                        clienteId, veiculoId, PECA_ID, INSUMO_ID))
                .when().post("/ordem-servico")
                .then().statusCode(201);

        List<LoggedRequest> catalogo = estoque.findAll(getRequestedFor(urlPathMatching("/catalogo/(pecas|insumos)")));
        List<LoggedRequest> reservas = estoque.findAll(putRequestedFor(urlPathMatching(RESERVAR_ITENS)));
        Assertions.assertEquals(2, catalogo.size());
        Assertions.assertEquals(1, reservas.size());

        // Catálogo (x2) e reserva saem da MESMA requisição: um único trace, com spans (ids) diferentes.
        List<LoggedRequest> todas = new java.util.ArrayList<>(catalogo);
        todas.addAll(reservas);
        var traceIds = todas.stream().map(PropagacaoDeTraceEstoqueIntegrationTest::traceId).distinct().toList();
        var spanIds = todas.stream().map(PropagacaoDeTraceEstoqueIntegrationTest::spanId).distinct().toList();
        Assertions.assertEquals(1, traceIds.size(), "as 3 chamadas deveriam pertencer ao mesmo trace: " + traceIds);
        Assertions.assertEquals(3, spanIds.size(), "cada chamada HTTP deveria ter o próprio span: " + spanIds);
    }

    private static String traceId(LoggedRequest chamada) {
        return partes(chamada).group(1);
    }

    private static String spanId(LoggedRequest chamada) {
        return partes(chamada).group(2);
    }

    private static java.util.regex.Matcher partes(LoggedRequest chamada) {
        String traceparent = chamada.getHeader("traceparent");
        Assertions.assertNotNull(traceparent, "faltou o cabeçalho traceparent em " + chamada.getUrl());
        var m = TRACEPARENT.matcher(traceparent);
        Assertions.assertTrue(m.matches(), "traceparent fora do formato W3C: " + traceparent);
        return m;
    }
}
