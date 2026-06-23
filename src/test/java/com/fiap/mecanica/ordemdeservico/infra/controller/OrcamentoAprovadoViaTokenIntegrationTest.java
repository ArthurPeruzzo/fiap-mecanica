package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.LinkAprovacaoOrcamentoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

class OrcamentoAprovadoViaTokenIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private String obterTokenLinkDeOrdemAguardandoAprovacao(Long ordemId) {
        return linkAprovacaoOrcamentoRepository.findByOrdemServicoId(ordemId)
                .orElseThrow()
                .getToken();
    }

    @Test
    void shouldAprovarOrcamentoViaTokenSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        String token = obterTokenLinkDeOrdemAguardandoAprovacao(ordemId);

        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/aprovar/" + token)
                .then().statusCode(204);

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.EM_EXECUCAO, ordem.getStatus());
        Assertions.assertNotNull(ordem.getDataAprovacao());

        LinkAprovacaoOrcamentoEntity link = linkAprovacaoOrcamentoRepository
                .findByOrdemServicoId(ordemId).orElseThrow();
        Assertions.assertNotNull(link.getDataUtilizacao());
    }

    @Test
    void shouldRetornar404QuandoTokenNaoExiste() {
        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/aprovar/" + UUID.randomUUID())
                .then().statusCode(404);
    }

    @Test
    void shouldRetornar410QuandoTokenJaUtilizado() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        String token = obterTokenLinkDeOrdemAguardandoAprovacao(ordemId);

        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/aprovar/" + token)
                .then().statusCode(204);

        RestAssured.given()
                .when().get("/ordem-servico/orcamento/externo/aprovar/" + token)
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
                .when().get("/ordem-servico/orcamento/externo/aprovar/" + token)
                .then().statusCode(410);
    }
}
