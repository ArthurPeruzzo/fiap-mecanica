package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

class ConsultarStatusOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    @Test
    void shouldReturn200WithStatusRecebidaWhenOrdemRecemCriada() {
        String tokenAtendente = obterTokenAtendente();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().get("/ordem-servico/" + ordemId + "/status")
                .then().statusCode(200)
                .body("id", equalTo(ordemId.intValue()))
                .body("status", equalTo(StatusOrdemDeServico.RECEBIDA.name()));
    }

    @Test
    void shouldReturn200WithStatusEmDiagnosticoWhenDiagnosticoIniciado() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().get("/ordem-servico/" + ordemId + "/status")
                .then().statusCode(200)
                .body("status", equalTo(StatusOrdemDeServico.EM_DIAGNOSTICO.name()));
    }

    @Test
    void shouldReturn200WithStatusAguardandoAprovacaoWhenOrcamentoEnviado() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);

        String tokenAdmin = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "77722244432");

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .when().get("/ordem-servico/" + ordemId + "/status")
                .then().statusCode(200)
                .body("status", equalTo(StatusOrdemDeServico.AGUARDANDO_APROVACAO.name()));
    }

    @Test
    void shouldReturn404WhenOrdemDeServicoNotFound() {
        String tokenAtendente = obterTokenAtendente();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().get("/ordem-servico/999/status")
                .then().statusCode(404)
                .body("message", equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() {
        RestAssured.given()
                .when().get("/ordem-servico/1/status")
                .then().statusCode(401);
    }
}
