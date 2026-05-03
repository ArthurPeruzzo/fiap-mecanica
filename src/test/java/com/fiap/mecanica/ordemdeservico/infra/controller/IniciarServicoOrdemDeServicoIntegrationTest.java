package com.fiap.mecanica.ordemdeservico.infra.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IniciarServicoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private record OrdemComServico(Long ordemId, Long servicoId) {}

    private OrdemComServico criarOrdemEmExecucaoComServico(String tokenAtendente, String tokenMecanico) {
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

        return new OrdemComServico(ordemId, servicoId);
    }

    @Test
    void shouldIniciarServicoSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        var ordemComServico = criarOrdemEmExecucaoComServico(tokenAtendente, tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemComServico.ordemId() + "/servicos/" + ordemComServico.servicoId() + "/iniciar")
                .then().statusCode(204);

        var row = jdbcTemplate.queryForMap(
                "SELECT status, data_inicio_execucao FROM ordem_servico_servico WHERE ordem_servico_id = ? AND servico_id = ?",
                ordemComServico.ordemId(), ordemComServico.servicoId());
        Assertions.assertEquals("EM_EXECUCAO", row.get("status"));
        Assertions.assertNotNull(row.get("data_inicio_execucao"));
    }
}
