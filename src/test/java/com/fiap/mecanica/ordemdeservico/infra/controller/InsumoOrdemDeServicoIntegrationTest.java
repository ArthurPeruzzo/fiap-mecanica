package com.fiap.mecanica.ordemdeservico.infra.controller;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InsumoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private int contarVinculosInsumoNoBanco(Long ordemId, Long insumoId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico_insumo WHERE ordem_servico_id = ? AND insumo_id = ?",
                Integer.class, ordemId, insumoId);
        return count != null ? count : 0;
    }

    private Integer obterQuantidadeInsumoNoBanco(Long ordemId, Long insumoId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade FROM ordem_servico_insumo WHERE ordem_servico_id = ? AND insumo_id = ?",
                Integer.class, ordemId, insumoId);
    }

    // --- vincularInsumo ---

    @Test
    void shouldVincularInsumoSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosInsumoNoBanco(ordemId, insumoId));
        Assertions.assertEquals(4, obterQuantidadeInsumoNoBanco(ordemId, insumoId));
    }

    @Test
    void shouldSomarQuantidadeWhenInsumoVinculadoJaExiste() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosInsumoNoBanco(ordemId, insumoId));
        Assertions.assertEquals(7, obterQuantidadeInsumoNoBanco(ordemId, insumoId));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(1);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemStatusNaoPermiteVincularInsumo() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível vincular insumos se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/9999/insumos/" + insumoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Insumo não encontrado"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNullOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsZeroOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":0}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser no mínimo 1"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnVincularInsumo() {
        RestAssured.given().contentType("application/json")
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/1/insumos/1")
                .then().statusCode(401);
    }

    // --- desvincularInsumo ---

    @Test
    void shouldDesvincularInsumoParcialmenteSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosInsumoNoBanco(ordemId, insumoId));
        Assertions.assertEquals(3, obterQuantidadeInsumoNoBanco(ordemId, insumoId));
    }

    @Test
    void shouldDesvincularInsumoIntegralmenteEDeletarVinculo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        Assertions.assertEquals(0, contarVinculosInsumoNoBanco(ordemId, insumoId));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaDesvincularInsumo() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível desvincular insumos se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenInsumoNaoVinculadoOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Insumo não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeMaiorQueVinculadaOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":10}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Quantidade a desvincular é maior que a quantidade vinculada"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/9999/insumos/" + insumoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Insumo não encontrado"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNullOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnDesvincularInsumo() {
        RestAssured.given().contentType("application/json")
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/1/insumos/1")
                .then().statusCode(401);
    }
}
