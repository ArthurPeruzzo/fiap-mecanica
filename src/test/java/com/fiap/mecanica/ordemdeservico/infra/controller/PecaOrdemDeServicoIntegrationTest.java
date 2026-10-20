package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.entity.VeiculoEntity;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

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

    private Integer obterEstoquePecaNoBanco(Long pecaId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade_estoque FROM peca WHERE id = ?", Integer.class, pecaId);
    }

    private Integer obterReservadaPecaNoBanco(Long pecaId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade_reservada FROM peca WHERE id = ?", Integer.class, pecaId);
    }

    private Map<String, Object> obterReservaNoBanco(Long ordemId, Long pecaId) {
        return jdbcTemplate.queryForMap(
                "SELECT tipo_item, item_id, quantidade, status FROM reserva_estoque " +
                        "WHERE ordem_servico_id = ? AND tipo_item = 'PECA' AND item_id = ?",
                ordemId, pecaId);
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

    private Long criarOutraOrdemEmDiagnosticoERetornarId(String tokenAtendente, String tokenMecanico) {
        Long clienteId = clienteRepository.saveAndFlush(ClienteEntity.builder()
                .nome("José").cpf("98765432100").build()).getId();
        Long veiculoId = veiculoRepository.saveAndFlush(VeiculoEntity.builder()
                .placa("XYZ9876").modelo("Gol").ano(2018)
                .cliente(ClienteEntity.builder().id(clienteId).build()).build()).getId();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenAtendente)
                .body(String.format("{\"clienteId\":%d,\"veiculoId\":%d,\"descricao\":\"Revisão\"}", clienteId, veiculoId))
                .when().post("/ordem-servico")
                .then().statusCode(201);
        Long ordemId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM ordem_servico", Long.class);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);
        return ordemId;
    }

    // --- vincularPeca ---

    @Test
    void shouldVincularPecaSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosPecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(3, obterQuantidadePecaNoBanco(ordemId, pecaId));
    }

    @Test
    void shouldSomarQuantidadeWhenPecaVinculadaJaExiste() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

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
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(1);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemStatusNaoPermiteVincularPeca() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível vincular peças se a ordem de serviço não está em diagnóstico ou recebida"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/9999/pecas/" + pecaId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnVincularPeca() {
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
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsZeroOnVincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

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
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

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
    }

    @Test
    void shouldDesvincularPecaIntegralmenteEDeletarVinculo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

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
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaDesvincularPeca() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível desvincular peças se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenPecaNaoVinculadaOnDesvincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Peça não está vinculada à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeMaiorQueVinculadaOnDesvincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

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
        String tokenMecanico = obterTokenMecanico();
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/9999/pecas/" + pecaId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnDesvincularPeca() {
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
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

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
    void shouldReservarEstoqueAoVincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        vincularPeca(tokenMecanico, ordemId, pecaId, 3, 204);

        Assertions.assertEquals(10, obterEstoquePecaNoBanco(pecaId));
        Assertions.assertEquals(3, obterReservadaPecaNoBanco(pecaId));

        Map<String, Object> reserva = obterReservaNoBanco(ordemId, pecaId);
        Assertions.assertEquals("PECA", reserva.get("tipo_item"));
        Assertions.assertEquals(pecaId, ((Number) reserva.get("item_id")).longValue());
        Assertions.assertEquals(3, ((Number) reserva.get("quantidade")).intValue());
        Assertions.assertEquals("ATIVA", reserva.get("status"));
    }

    @Test
    void shouldAjustarReservaAoVincularEDesvincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        vincularPeca(tokenMecanico, ordemId, pecaId, 3, 204);
        vincularPeca(tokenMecanico, ordemId, pecaId, 2, 204);
        Assertions.assertEquals(5, obterReservadaPecaNoBanco(pecaId));
        Assertions.assertEquals(5, ((Number) obterReservaNoBanco(ordemId, pecaId).get("quantidade")).intValue());

        desvincularPeca(tokenMecanico, ordemId, pecaId, 2);
        Assertions.assertEquals(3, obterReservadaPecaNoBanco(pecaId));
        Assertions.assertEquals("ATIVA", obterReservaNoBanco(ordemId, pecaId).get("status"));

        desvincularPeca(tokenMecanico, ordemId, pecaId, 3);
        Assertions.assertEquals(0, obterReservadaPecaNoBanco(pecaId));
        Assertions.assertEquals(10, obterEstoquePecaNoBanco(pecaId));

        Map<String, Object> reserva = obterReservaNoBanco(ordemId, pecaId);
        Assertions.assertEquals(0, ((Number) reserva.get("quantidade")).intValue());
        Assertions.assertEquals("LIBERADA", reserva.get("status"));
    }

    @Test
    void shouldReturn422QuandoOutraOrdemJaReservouODisponivelDePeca() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemA = criarOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);
        Long ordemB = criarOutraOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);
        Long pecaId = criarPecaERetornarId(5);

        vincularPeca(tokenMecanico, ordemA, pecaId, 4, 204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/" + ordemB + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Estoque insuficiente para realizar a operação"));
        Assertions.assertEquals(0, contarVinculosPecaNoBanco(ordemB, pecaId));
        Assertions.assertEquals(4, obterReservadaPecaNoBanco(pecaId));

        vincularPeca(tokenMecanico, ordemB, pecaId, 1, 204);
        Assertions.assertEquals(5, obterReservadaPecaNoBanco(pecaId));
        Assertions.assertEquals(5, obterEstoquePecaNoBanco(pecaId));
    }

    @Test
    void shouldReturn422AoAtualizarEstoqueDePecaParaMenosQueOReservado() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);
        String tokenAdministrador = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "52998224725");

        vincularPeca(tokenMecanico, ordemId, pecaId, 4, 204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenAdministrador)
                .body(String.format("{\"nome\":\"Filtro de óleo\",\"descricao\":\"Filtro original\",\"preco\":45.00,\"quantidadeEstoque\":%d}", 3))
                .when().put("/peca/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("A quantidade em estoque não pode ser menor que a quantidade reservada para ordens de serviço"));
        Assertions.assertEquals(10, obterEstoquePecaNoBanco(pecaId));

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenAdministrador)
                .body(String.format("{\"nome\":\"Filtro de óleo\",\"descricao\":\"Filtro original\",\"preco\":45.00,\"quantidadeEstoque\":%d}", 4))
                .when().put("/peca/" + pecaId)
                .then().statusCode(204);
        Assertions.assertEquals(4, obterEstoquePecaNoBanco(pecaId));
        Assertions.assertEquals(4, obterReservadaPecaNoBanco(pecaId));
    }
}
