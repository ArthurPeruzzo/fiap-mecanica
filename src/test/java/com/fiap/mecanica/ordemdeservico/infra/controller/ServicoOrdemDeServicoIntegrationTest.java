package com.fiap.mecanica.ordemdeservico.infra.controller;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ServicoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private int contarVinculosNoBanco(Long ordemId, Long servicoId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico_servico WHERE ordem_servico_id = ? AND servico_id = ?",
                Integer.class, ordemId, servicoId);
        return count != null ? count : 0;
    }

    // --- vincularServico ---

    @Test
    void shouldVincularServicoSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosNoBanco(ordemId, servicoId));
    }

    @Test
    void shouldReturn422WhenServicoJaVinculado() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Este serviço já está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaVincular() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincular() {
        String tokenMecanico = obterTokenMecanico();
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/9999/servicos/" + servicoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnVincular() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Serviço não encontrado"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnVincular() {
        RestAssured.given()
                .when().put("/ordem-servico/1/servicos/1")
                .then().statusCode(401);
    }

    // --- desvincularServico ---

    @Test
    void shouldDesvincularServicoSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        Assertions.assertEquals(0, contarVinculosNoBanco(ordemId, servicoId));
    }

    @Test
    void shouldReturn422WhenServicoNaoVinculado() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Este serviço não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaDesvincular() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincular() {
        String tokenMecanico = obterTokenMecanico();
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/9999/servicos/" + servicoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnDesvincular() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/" + ordemId + "/servicos/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Serviço não encontrado"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnDesvincular() {
        RestAssured.given()
                .when().delete("/ordem-servico/1/servicos/1")
                .then().statusCode(401);
    }
}
