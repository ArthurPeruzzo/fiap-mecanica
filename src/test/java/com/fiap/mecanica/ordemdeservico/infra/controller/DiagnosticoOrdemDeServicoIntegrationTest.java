package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DiagnosticoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    @Test
    void shouldIniciarDiagnosticoSuccessfully() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(obterTokenAtendente(), clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + obterTokenMecanico())
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.EM_DIAGNOSTICO, ordem.getStatus());
        Assertions.assertNotNull(ordem.getMecanicoId());
        Assertions.assertNotNull(ordem.getDataInicioDiagnostico());
        Assertions.assertNull(ordem.getDataConclusaoDiagnostico());
    }

    @Test
    void shouldConcluirDiagnosticoSuccessfully() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemERetornarId(obterTokenAtendente(), clienteId, veiculoId);
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

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, ordem.getStatus());
        Assertions.assertNotNull(ordem.getDataInicioDiagnostico());
        Assertions.assertNotNull(ordem.getDataConclusaoDiagnostico());
    }

    @Test
    void shouldReturn422WhenSemServicosVinculadosOnConcluir() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemERetornarId(obterTokenAtendente(), clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao")
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível concluir o diagnóstico sem ao menos um serviço vinculado"));
    }

    @Test
    void shouldReturn422WhenMecanicoNaoEhResponsavelOnConcluir() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemERetornarId(obterTokenAtendente(), clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + obterTokenOutroMecanico())
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao")
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Somente o mecânico responsável pelo diagnóstico pode concluí-lo"));
    }
}
