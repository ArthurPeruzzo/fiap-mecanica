package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class DetalhamentoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    @Test
    void shouldReturnDetalhamentoOrdemDeServicoSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenAdmin = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "admin@test.com");

        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/ordem-servico/detalhamento")
                .then().statusCode(200)
                .body("content.size()", Matchers.equalTo(1))
                .body("content[0].nomeCliente", Matchers.equalTo("Maria Santos"))
                .body("content[0].veiculo", Matchers.equalTo("Civic 2020 ABC-1234"))
                .body("content[0].nomeAtendente", Matchers.equalTo("João Silva"))
                .body("content[0].nomeMecanico", Matchers.nullValue())
                .body("content[0].status", Matchers.equalTo("RECEBIDA"))
                .body("content[0].descricao", Matchers.equalTo("Barulho ao frear"))
                .body("content[0].servicos.size()", Matchers.equalTo(0))
                .body("content[0].pecas.size()", Matchers.equalTo(0))
                .body("content[0].insumos.size()", Matchers.equalTo(0))
                .body("totalElements", Matchers.equalTo(1))
                .body("totalPages", Matchers.equalTo(1));
    }
}
