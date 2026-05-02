package com.fiap.mecanica.ordemdeservico.infra.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FinalizarServicoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private record OrdemComServico(Long ordemId, Long servicoId) {}

    private OrdemComServico criarOrdemComServicoEmExecucao(String tokenAtendente, String tokenMecanico) {
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

        return new OrdemComServico(ordemId, servicoId);
    }

    @Test
    void shouldFinalizarServicoEFinalizarOSSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        var ordemComServico = criarOrdemComServicoEmExecucao(tokenAtendente, tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemComServico.ordemId() + "/servicos/" + ordemComServico.servicoId() + "/finalizar")
                .then().statusCode(204);

        var servicoRow = jdbcTemplate.queryForMap(
                "SELECT status, data_fim_execucao FROM ordem_servico_servico WHERE ordem_servico_id = ? AND servico_id = ?",
                ordemComServico.ordemId(), ordemComServico.servicoId());
        Assertions.assertEquals("FINALIZADO", servicoRow.get("status"));
        Assertions.assertNotNull(servicoRow.get("data_fim_execucao"));

        var ordemRow = jdbcTemplate.queryForMap(
                "SELECT status FROM ordem_servico WHERE id = ?",
                ordemComServico.ordemId());
        Assertions.assertEquals("FINALIZADA", ordemRow.get("status"));

        var dataFinalizacaoRow = jdbcTemplate.queryForMap(
                "SELECT data_finalizacao FROM ordem_servico WHERE id = ?",
                ordemComServico.ordemId());
        Assertions.assertNotNull(dataFinalizacaoRow.get("data_finalizacao"));
    }
}
