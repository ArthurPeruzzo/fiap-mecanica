package com.fiap.mecanica.ordemdeservico.infra.controller;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

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

    private void vincularInsumo(String tokenMecanico, Long ordemId, Long insumoId, int quantidade, int statusEsperado) {
        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", quantidade))
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(statusEsperado);
    }

    private void desvincularInsumo(String tokenMecanico, Long ordemId, Long insumoId, int quantidade) {
        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", quantidade))
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);
    }

    // --- vincularInsumo ---

    @Test
    void shouldVincularInsumoSuccessfully() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosInsumoNoBanco(ordemId, insumoId));
        Assertions.assertEquals(4, obterQuantidadeInsumoNoBanco(ordemId, insumoId));
        estoque.verify(exactly(1), putRequestedFor(urlEqualTo("/reserva/ordem-servico/" + ordemId + "/itens"))
                .withRequestBody(equalToJson(String.format(
                        "{\"itens\":[{\"tipoItem\":\"INSUMO\",\"itemId\":%d,\"quantidadeTotal\":4}]}", insumoId))));
    }

    @Test
    void shouldSomarQuantidadeWhenInsumoVinculadoJaExiste() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

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
        Assertions.assertEquals(7, quantidadesReservadasNoEstoque().getLast());
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularInsumo() {
        stubCatalogo();
        stubReservaStatus(422);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Estoque insuficiente para realizar a operação"));

        Assertions.assertEquals(0, contarVinculosInsumoNoBanco(ordemId, insumoId));
    }

    @Test
    void shouldReturn503AndKeepOrdemIntactWhenEstoqueIndisponivelOnVincularInsumo() {
        stubCatalogo();
        stubReservaStatus(500);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        vincularInsumo(tokenMecanico, ordemId, INSUMO_ID, 3, 503);

        Assertions.assertEquals(0, contarVinculosInsumoNoBanco(ordemId, INSUMO_ID));
    }

    @Test
    void shouldReturn422WhenOrdemStatusNaoPermiteVincularInsumo() {
        stubCatalogo();
        stubReservaOk();
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível vincular insumos se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularInsumo() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/9999/insumos/" + insumoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnVincularInsumo() {
        stubCatalogoVazio();
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
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsZeroOnVincularInsumo() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

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
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

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
        Assertions.assertEquals(3, quantidadesReservadasNoEstoque().getLast());
    }

    @Test
    void shouldDesvincularInsumoIntegralmenteEDeletarVinculo() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

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
        Assertions.assertEquals(0, quantidadesReservadasNoEstoque().getLast());
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaDesvincularInsumo() {
        stubCatalogo();
        stubReservaOk();
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível desvincular insumos se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenInsumoNaoVinculadoOnDesvincularInsumo() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Insumo não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeMaiorQueVinculadaOnDesvincularInsumo() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

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
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/9999/insumos/" + insumoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnDesvincularInsumo() {
        stubCatalogoVazio();
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
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = INSUMO_ID;

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

    // --- reserva de estoque ---

    @Test
    void shouldReenviarAReservaTotalAoVincularEDesvincularInsumo() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        vincularInsumo(tokenMecanico, ordemId, INSUMO_ID, 3, 204);
        vincularInsumo(tokenMecanico, ordemId, INSUMO_ID, 2, 204);
        desvincularInsumo(tokenMecanico, ordemId, INSUMO_ID, 2);
        desvincularInsumo(tokenMecanico, ordemId, INSUMO_ID, 3);

        Assertions.assertEquals(List.of(3, 5, 3, 0), quantidadesReservadasNoEstoque());
        Assertions.assertEquals(0, contarVinculosInsumoNoBanco(ordemId, INSUMO_ID));
    }
}
