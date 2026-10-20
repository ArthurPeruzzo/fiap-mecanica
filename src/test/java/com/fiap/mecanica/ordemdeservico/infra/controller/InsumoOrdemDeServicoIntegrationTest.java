package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.entity.VeiculoEntity;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

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

    private Integer obterEstoqueInsumoNoBanco(Long insumoId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade_estoque FROM insumo WHERE id = ?", Integer.class, insumoId);
    }

    private Integer obterReservadaInsumoNoBanco(Long insumoId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade_reservada FROM insumo WHERE id = ?", Integer.class, insumoId);
    }

    private Map<String, Object> obterReservaNoBanco(Long ordemId, Long insumoId) {
        return jdbcTemplate.queryForMap(
                "SELECT tipo_item, item_id, quantidade, status FROM reserva_estoque " +
                        "WHERE ordem_servico_id = ? AND tipo_item = 'INSUMO' AND item_id = ?",
                ordemId, insumoId);
    }

    private void vincularInsumo(String tokenMecanico, Long ordemId, Long insumoId, int quantidade, int statusEsperado) {
        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":" + quantidade + "}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(statusEsperado);
    }

    private void desvincularInsumo(String tokenMecanico, Long ordemId, Long insumoId, int quantidade) {
        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":" + quantidade + "}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
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

    // --- reserva de estoque ---

    @Test
    void shouldReservarEstoqueAoVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        vincularInsumo(tokenMecanico, ordemId, insumoId, 3, 204);

        Assertions.assertEquals(10, obterEstoqueInsumoNoBanco(insumoId));
        Assertions.assertEquals(3, obterReservadaInsumoNoBanco(insumoId));

        Map<String, Object> reserva = obterReservaNoBanco(ordemId, insumoId);
        Assertions.assertEquals("INSUMO", reserva.get("tipo_item"));
        Assertions.assertEquals(insumoId, ((Number) reserva.get("item_id")).longValue());
        Assertions.assertEquals(3, ((Number) reserva.get("quantidade")).intValue());
        Assertions.assertEquals("ATIVA", reserva.get("status"));
    }

    @Test
    void shouldAjustarReservaAoVincularEDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        vincularInsumo(tokenMecanico, ordemId, insumoId, 3, 204);
        vincularInsumo(tokenMecanico, ordemId, insumoId, 2, 204);
        Assertions.assertEquals(5, obterReservadaInsumoNoBanco(insumoId));
        Assertions.assertEquals(5, ((Number) obterReservaNoBanco(ordemId, insumoId).get("quantidade")).intValue());

        desvincularInsumo(tokenMecanico, ordemId, insumoId, 2);
        Assertions.assertEquals(3, obterReservadaInsumoNoBanco(insumoId));
        Assertions.assertEquals("ATIVA", obterReservaNoBanco(ordemId, insumoId).get("status"));

        desvincularInsumo(tokenMecanico, ordemId, insumoId, 3);
        Assertions.assertEquals(0, obterReservadaInsumoNoBanco(insumoId));
        Assertions.assertEquals(10, obterEstoqueInsumoNoBanco(insumoId));

        Map<String, Object> reserva = obterReservaNoBanco(ordemId, insumoId);
        Assertions.assertEquals(0, ((Number) reserva.get("quantidade")).intValue());
        Assertions.assertEquals("LIBERADA", reserva.get("status"));
    }

    @Test
    void shouldReturn422QuandoOutraOrdemJaReservouODisponivelDeInsumo() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemA = criarOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);
        Long ordemB = criarOutraOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);
        Long insumoId = criarInsumoERetornarId(5);

        vincularInsumo(tokenMecanico, ordemA, insumoId, 4, 204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/" + ordemB + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Estoque insuficiente para realizar a operação"));
        Assertions.assertEquals(0, contarVinculosInsumoNoBanco(ordemB, insumoId));
        Assertions.assertEquals(4, obterReservadaInsumoNoBanco(insumoId));

        vincularInsumo(tokenMecanico, ordemB, insumoId, 1, 204);
        Assertions.assertEquals(5, obterReservadaInsumoNoBanco(insumoId));
        Assertions.assertEquals(5, obterEstoqueInsumoNoBanco(insumoId));
    }

    @Test
    void shouldReturn422AoAtualizarEstoqueDeInsumoParaMenosQueOReservado() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);
        String tokenAdministrador = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "52998224725");

        vincularInsumo(tokenMecanico, ordemId, insumoId, 4, 204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenAdministrador)
                .body(String.format("{\"nome\":\"Óleo motor 5W30\",\"descricao\":\"Óleo sintético\",\"preco\":35.00,\"quantidadeEstoque\":%d,\"unidadeMedida\":\"LITRO\"}", 3))
                .when().put("/insumo/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("A quantidade em estoque não pode ser menor que a quantidade reservada para ordens de serviço"));
        Assertions.assertEquals(10, obterEstoqueInsumoNoBanco(insumoId));

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenAdministrador)
                .body(String.format("{\"nome\":\"Óleo motor 5W30\",\"descricao\":\"Óleo sintético\",\"preco\":35.00,\"quantidadeEstoque\":%d,\"unidadeMedida\":\"LITRO\"}", 4))
                .when().put("/insumo/" + insumoId)
                .then().statusCode(204);
        Assertions.assertEquals(4, obterEstoqueInsumoNoBanco(insumoId));
        Assertions.assertEquals(4, obterReservadaInsumoNoBanco(insumoId));
    }
}
