package com.fiap.mecanica.ordemdeservico.infra.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EntregarOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private Long criarOrdemFinalizadaERetornarId(String tokenAtendente, String tokenMecanico) {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
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

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/aprovar/" + ordemId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/servicos/" + servicoId + "/iniciar")
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/servicos/" + servicoId + "/finalizar")
                .then().statusCode(204);

        return ordemId;
    }

    @Test
    void shouldEntregarOrdemDeServicoSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemFinalizadaERetornarId(tokenAtendente, tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().patch("/ordem-servico/" + ordemId + "/entregar")
                .then().statusCode(204);

        var ordemRow = jdbcTemplate.queryForMap(
                "SELECT status, data_entrega FROM ordem_servico WHERE id = ?", ordemId);
        Assertions.assertEquals("ENTREGUE", ordemRow.get("status"));
        Assertions.assertNotNull(ordemRow.get("data_entrega"));
    }
}
