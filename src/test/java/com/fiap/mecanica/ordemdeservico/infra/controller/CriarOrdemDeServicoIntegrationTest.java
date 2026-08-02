package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CriarOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

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
