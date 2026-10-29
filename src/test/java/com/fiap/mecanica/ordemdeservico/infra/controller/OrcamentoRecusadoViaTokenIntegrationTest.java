package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.LinkAprovacaoOrcamentoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

class OrcamentoRecusadoViaTokenIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private String obterTokenLinkDeOrdemAguardandoAprovacao(Long ordemId) {
        return linkAprovacaoOrcamentoRepository.findByOrdemServicoId(ordemId)
                .orElseThrow()
                .getToken();
    }

    @Test
    void shouldRecusarOrcamentoViaTokenSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        String token = obterTokenLinkDeOrdemAguardandoAprovacao(ordemId);

        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/recusar/" + token)
                .then().statusCode(204);

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.CANCELADA, ordem.getStatus());
        Assertions.assertNotNull(ordem.getDataCancelamento());

        LinkAprovacaoOrcamentoEntity link = linkAprovacaoOrcamentoRepository
                .findByOrdemServicoId(ordemId).orElseThrow();
        Assertions.assertNotNull(link.getDataUtilizacao());
    }

    @Test
    void shouldLiberarReservaAoRecusarViaToken() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();

        Long pecaId = PECA_ID;
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .contentType("application/json")
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao")
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/envio/" + ordemId)
                .then().statusCode(204);

        estoque.verify(exactly(0), postRequestedFor(urlPathMatching(LIBERAR)));

        String token = obterTokenLinkDeOrdemAguardandoAprovacao(ordemId);
        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/recusar/" + token)
                .then().statusCode(204);

        estoque.verify(exactly(1), postRequestedFor(urlEqualTo("/reserva/ordem-servico/" + ordemId + "/liberar")));
    }

    @Test
    void shouldRetornar404QuandoTokenNaoExiste() {
        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/recusar/" + UUID.randomUUID())
                .then().statusCode(404);
    }

    @Test
    void shouldRetornar410QuandoTokenJaUtilizado() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        String token = obterTokenLinkDeOrdemAguardandoAprovacao(ordemId);

        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/recusar/" + token)
                .then().statusCode(204);

        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/recusar/" + token)
                .then().statusCode(410);
    }

    @Test
    void shouldRetornar410QuandoTokenExpirado() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        String token = obterTokenLinkDeOrdemAguardandoAprovacao(ordemId);

        jdbcTemplate.update(
                "UPDATE link_aprovacao_orcamento SET data_expiracao = ? WHERE token = ?",
                LocalDateTime.now().minusDays(1), token);

        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/recusar/" + token)
                .then().statusCode(410);
    }
}
