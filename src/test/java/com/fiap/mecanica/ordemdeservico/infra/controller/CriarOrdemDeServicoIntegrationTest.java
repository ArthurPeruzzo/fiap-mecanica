package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

/**
 * O fiap-estoque é simulado com o WireMock de AbstractContainer: o RestClient real chama um servidor HTTP local, então o
 * teste cobre rota, corpo, mapeamento de status e timeout (estoque.fake=false desliga o fake local).
 */
class CriarOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private int contarOrdensNoBanco() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ordem_servico", Integer.class);
    }

    private int contarVinculosNoBanco() {
        return jdbcTemplate.queryForObject(
                "SELECT (SELECT COUNT(*) FROM ordem_servico_peca) + (SELECT COUNT(*) FROM ordem_servico_insumo)",
                Integer.class);
    }

    private String corpoCriacao(Long clienteId, Long veiculoId, Long pecaId, Long insumoId) {
        return String.format("""
                {"clienteId":%d,"veiculoId":%d,"descricao":"Barulho ao frear",
                 "pecas":[{"id":%d,"quantidade":2}],"insumos":[{"id":%d,"quantidade":3}]}""",
                clienteId, veiculoId, pecaId, insumoId);
    }

    @Test
    void shouldCreateOrdemDeServicoSuccessfully() {
        String token = obterTokenAtendente();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"veiculoId\":%d,\"descricao\":\"Barulho ao frear\"}", clienteId, veiculoId))
                .when().post("/ordem-servico")
                .then().statusCode(201);

        var ordens = ordemDeServicoRepository.findAll();
        Assertions.assertEquals(1, ordens.size());
        OrdemDeServicoEntity ordem = ordens.getFirst();
        Assertions.assertEquals(StatusOrdemDeServico.RECEBIDA, ordem.getStatus());
        Assertions.assertEquals(clienteId, ordem.getClienteId());
        Assertions.assertEquals(veiculoId, ordem.getVeiculoId());
        Assertions.assertEquals("Barulho ao frear", ordem.getDescricao());
        Assertions.assertNotNull(ordem.getDataCriacao());
        Assertions.assertNull(ordem.getDataInicioDiagnostico());
        Assertions.assertNull(ordem.getDataConclusaoDiagnostico());
    }

    @Test
    void shouldReservarPecasEInsumosNoEstoqueEmUmaUnicaChamada() {
        estoque.stubFor(put(urlPathMatching(RESERVAR_ITENS)).willReturn(aResponse().withStatus(204)));
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        stubCatalogo();
        Long pecaId = PECA_ID;
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + obterTokenAtendente())
                .body(corpoCriacao(clienteId, veiculoId, pecaId, insumoId))
                .when().post("/ordem-servico")
                .then().statusCode(201);

        Long ordemId = ordemDeServicoRepository.findAll().getFirst().getId();
        estoque.verify(exactly(1), putRequestedFor(urlEqualTo("/reserva/ordem-servico/" + ordemId + "/itens"))
                .withRequestBody(equalToJson(String.format(
                        "{\"itens\":[{\"tipoItem\":\"PECA\",\"itemId\":%d,\"quantidadeTotal\":2},"
                                + "{\"tipoItem\":\"INSUMO\",\"itemId\":%d,\"quantidadeTotal\":3}]}", pecaId, insumoId))));
        Assertions.assertEquals(2, contarVinculosNoBanco());
    }

    @Test
    void shouldNaoChamarEstoqueQuandoNaoHaItens() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + obterTokenAtendente())
                .body(String.format("{\"clienteId\":%d,\"veiculoId\":%d,\"descricao\":\"Barulho ao frear\"}", clienteId, veiculoId))
                .when().post("/ordem-servico")
                .then().statusCode(201);

        Assertions.assertTrue(estoque.getAllServeEvents().isEmpty());
    }

    @Test
    void shouldReturn422ENaoGravarOrdemQuandoEstoqueInsuficiente() {
        estoque.stubFor(put(urlPathMatching(RESERVAR_ITENS)).willReturn(aResponse().withStatus(422)));
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        stubCatalogo();
        Long pecaId = PECA_ID;
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + obterTokenAtendente())
                .body(corpoCriacao(clienteId, veiculoId, pecaId, insumoId))
                .when().post("/ordem-servico")
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Estoque insuficiente para realizar a operação"));

        Assertions.assertEquals(0, contarOrdensNoBanco());
        Assertions.assertEquals(0, contarVinculosNoBanco());
    }

    @Test
    void shouldReturn503ENaoGravarOrdemQuandoEstoqueFalha() {
        estoque.stubFor(put(urlPathMatching(RESERVAR_ITENS)).willReturn(aResponse().withStatus(500)));
        estoque.stubFor(post(urlPathMatching(LIBERAR)).willReturn(aResponse().withStatus(204)));
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        stubCatalogo();
        Long pecaId = PECA_ID;
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + obterTokenAtendente())
                .body(corpoCriacao(clienteId, veiculoId, pecaId, insumoId))
                .when().post("/ordem-servico")
                .then().statusCode(503);

        Assertions.assertEquals(0, contarOrdensNoBanco());
        Assertions.assertEquals(0, contarVinculosNoBanco());
    }

    @Test
    void shouldReturn503ELiberarReservaQuandoEstoqueDemoraMaisQueOTimeout() {
        estoque.stubFor(put(urlPathMatching(RESERVAR_ITENS)).willReturn(aResponse().withStatus(204).withFixedDelay(2000)));
        estoque.stubFor(post(urlPathMatching(LIBERAR)).willReturn(aResponse().withStatus(204)));
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        stubCatalogo();
        Long pecaId = PECA_ID;
        Long insumoId = INSUMO_ID;

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + obterTokenAtendente())
                .body(corpoCriacao(clienteId, veiculoId, pecaId, insumoId))
                .when().post("/ordem-servico")
                .then().statusCode(503);

        Assertions.assertEquals(0, contarOrdensNoBanco());
        // a resposta se perdeu, mas o estoque pode ter reservado: a compensação libera
        estoque.verify(exactly(1), postRequestedFor(urlPathMatching(LIBERAR)));
    }

    @Test
    void shouldReturn404ENaoReservarQuandoPecaNaoExisteNoCatalogo() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[]")));
        estoque.stubFor(get(urlPathEqualTo("/catalogo/insumos"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":" + INSUMO_ID + ",\"preco\":35.00}]")));
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + obterTokenAtendente())
                .body(corpoCriacao(clienteId, veiculoId, PECA_ID, INSUMO_ID))
                .when().post("/ordem-servico")
                .then().statusCode(404);

        Assertions.assertEquals(0, contarOrdensNoBanco());
        estoque.verify(exactly(0), putRequestedFor(urlPathMatching(RESERVAR_ITENS)));
    }

    @Test
    void shouldReturn503ENaoGravarOrdemQuandoCatalogoIndisponivel() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas")).willReturn(aResponse().withStatus(500)));
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + obterTokenAtendente())
                .body(corpoCriacao(clienteId, veiculoId, PECA_ID, INSUMO_ID))
                .when().post("/ordem-servico")
                .then().statusCode(503);

        Assertions.assertEquals(0, contarOrdensNoBanco());
        estoque.verify(exactly(0), putRequestedFor(urlPathMatching(RESERVAR_ITENS)));
    }

    @Test
    void shouldReturn422WhenOrdemAbertaExistsForVeiculo() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        String token = obterTokenAtendente();
        String body = String.format("{\"clienteId\":%d,\"veiculoId\":%d,\"descricao\":\"Barulho ao frear\"}", clienteId, veiculoId);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when().post("/ordem-servico")
                .then().statusCode(201);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when().post("/ordem-servico")
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Já existe uma ordem de serviço aberta para este veículo"));
    }

    @Test
    void shouldReturn400WhenClienteIdIsNull() {
        String token = obterTokenAtendente();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"veiculoId\":1,\"descricao\":\"Barulho ao frear\"}")
                .when().post("/ordem-servico")
                .then().statusCode(400)
                .body("clienteId", Matchers.equalTo("O cliente deve ser informado"));
    }

    @Test
    void shouldReturn400WhenVeiculoIdIsNull() {
        String token = obterTokenAtendente();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1,\"descricao\":\"Barulho ao frear\"}")
                .when().post("/ordem-servico")
                .then().statusCode(400)
                .body("veiculoId", Matchers.equalTo("O veículo deve ser informado"));
    }

    @Test
    void shouldReturn400WhenDescricaoIsNull() {
        String token = obterTokenAtendente();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1,\"veiculoId\":1}")
                .when().post("/ordem-servico")
                .then().statusCode(400)
                .body("descricao", Matchers.equalTo("A descrição deve ser informada"));
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        RestAssured.given().contentType("application/json")
                .body("{\"clienteId\":1,\"veiculoId\":1,\"descricao\":\"Barulho ao frear\"}")
                .when().post("/ordem-servico")
                .then().statusCode(401);
    }

    @Test
    void shouldReturn403WhenUserIsNotAtendente() {
        String token = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "77722244432");

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1,\"veiculoId\":1,\"descricao\":\"Barulho ao frear\"}")
                .when().post("/ordem-servico")
                .then().statusCode(403);
    }
}
