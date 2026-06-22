package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.LinkAprovacaoOrcamentoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

class EnviarOrcamentoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private Long criarOrdemDiagnosticoConcluidoERetornarId(String tokenAtendente, String tokenMecanico) {
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao")
                .then().statusCode(204);

        return ordemId;
    }

    @Test
    void shouldEnviarOrcamentoSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemDiagnosticoConcluidoERetornarId(tokenAtendente, tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/envio/" + ordemId)
                .then().statusCode(204);

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.AGUARDANDO_APROVACAO, ordem.getStatus());
        Assertions.assertNotNull(ordem.getDataEnvioOrcamento());

        LinkAprovacaoOrcamentoEntity link = linkAprovacaoOrcamentoRepository
                .findByOrdemServicoId(ordemId).orElseThrow();
        Assertions.assertDoesNotThrow(() -> UUID.fromString(link.getToken()));
        Assertions.assertNull(link.getDataUtilizacao());
        LocalDateTime agora = LocalDateTime.now();
        Assertions.assertTrue(link.getDataExpiracao().isAfter(agora.plusDays(2)));
        Assertions.assertTrue(link.getDataExpiracao().isBefore(agora.plusDays(4)));
    }

    @Test
    void shouldRetornar404QuandoOrdemNaoExiste() {
        String tokenAtendente = obterTokenAtendente();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/envio/999")
                .then().statusCode(404);

        Assertions.assertEquals(0, linkAprovacaoOrcamentoRepository.count());
    }

    @Test
    void shouldRetornar409QuandoStatusInvalido() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/envio/" + ordemId)
                .then().statusCode(422);

        Assertions.assertEquals(0, linkAprovacaoOrcamentoRepository.count());
    }
}
