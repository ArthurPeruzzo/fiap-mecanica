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

class PecaOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private int contarVinculosPecaNoBanco(Long ordemId, Long pecaId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico_peca WHERE ordem_servico_id = ? AND peca_id = ?",
                Integer.class, ordemId, pecaId);
        return count != null ? count : 0;
    }

    private Integer obterQuantidadePecaNoBanco(Long ordemId, Long pecaId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade FROM ordem_servico_peca WHERE ordem_servico_id = ? AND peca_id = ?",
                Integer.class, ordemId, pecaId);
    }

    private void vincularPeca(String tokenMecanico, Long ordemId, Long pecaId, int quantidade, int statusEsperado) {
        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", quantidade))
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(statusEsperado);
    }

    private void desvincularPeca(String tokenMecanico, Long ordemId, Long pecaId, int quantidade) {
        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", quantidade))
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);
    }

    // --- vincularPeca ---

    @Test
    void shouldVincularPecaSuccessfully() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosPecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(3, obterQuantidadePecaNoBanco(ordemId, pecaId));
        estoque.verify(exactly(1), putRequestedFor(urlEqualTo("/reserva/ordem-servico/" + ordemId + "/itens"))
                .withRequestBody(equalToJson(String.format(
                        "{\"itens\":[{\"tipoItem\":\"PECA\",\"itemId\":%d,\"quantidadeTotal\":3}]}", pecaId))));
    }

    @Test
    void shouldSomarQuantidadeWhenPecaVinculadaJaExiste() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosPecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(5, obterQuantidadePecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(List.of(3, 5), quantidadesReservadasNoEstoque());
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularPeca() {
        stubCatalogo();
        stubReservaStatus(422);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Estoque insuficiente para realizar a operação"));

        Assertions.assertEquals(0, contarVinculosPecaNoBanco(ordemId, pecaId));
    }

    @Test
    void shouldReturn503AndKeepOrdemIntactWhenEstoqueIndisponivelOnVincularPeca() {
        stubCatalogo();
        stubReservaStatus(500);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        vincularPeca(tokenMecanico, ordemId, PECA_ID, 3, 503);

        Assertions.assertEquals(0, contarVinculosPecaNoBanco(ordemId, PECA_ID));
    }

    @Test
    void shouldReturn422WhenOrdemStatusNaoPermiteVincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível vincular peças se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/9999/pecas/" + pecaId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnVincularPeca() {
        stubCatalogoVazio();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Peça não encontrada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNullOnVincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsZeroOnVincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":0}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser no mínimo 1"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnVincularPeca() {
        RestAssured.given().contentType("application/json")
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/1/pecas/1")
                .then().statusCode(401);
    }

    // --- desvincularPeca ---

    @Test
    void shouldDesvincularPecaParcialmenteSuccessfully() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosPecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(3, obterQuantidadePecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(List.of(5, 3), quantidadesReservadasNoEstoque());
    }

    @Test
    void shouldDesvincularPecaIntegralmenteEDeletarVinculo() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        Assertions.assertEquals(0, contarVinculosPecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(List.of(3, 0), quantidadesReservadasNoEstoque());
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaDesvincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível desvincular peças se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenPecaNaoVinculadaOnDesvincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Peça não está vinculada à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeMaiorQueVinculadaOnDesvincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":10}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Quantidade a desvincular é maior que a quantidade vinculada"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/9999/pecas/" + pecaId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnDesvincularPeca() {
        stubCatalogoVazio();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Peça não encontrada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNullOnDesvincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = PECA_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnDesvincularPeca() {
        RestAssured.given().contentType("application/json")
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/1/pecas/1")
                .then().statusCode(401);
    }

    // --- reserva de estoque ---

    @Test
    void shouldReenviarAReservaTotalAoVincularEDesvincularPeca() {
        stubCatalogo();
        stubReservaOk();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        vincularPeca(tokenMecanico, ordemId, PECA_ID, 3, 204);
        vincularPeca(tokenMecanico, ordemId, PECA_ID, 2, 204);
        desvincularPeca(tokenMecanico, ordemId, PECA_ID, 2);
        desvincularPeca(tokenMecanico, ordemId, PECA_ID, 3);

        Assertions.assertEquals(List.of(3, 5, 3, 0), quantidadesReservadasNoEstoque());
        Assertions.assertEquals(0, contarVinculosPecaNoBanco(ordemId, PECA_ID));
    }
}
