package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@TestPropertySource(properties = "estoque.fake=false")
class OrcamentoRecusadoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    @BeforeEach
    void stubsDoEstoque() {
        stubCatalogo();
        stubReservaOk();
        stubConfirmarEliberarOk();
    }

    private int contarVinculosPecaNoBanco(Long ordemId, Long pecaId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico_peca WHERE ordem_servico_id = ? AND peca_id = ?",
                Integer.class, ordemId, pecaId);
        return count != null ? count : 0;
    }

    private int contarVinculosInsumoNoBanco(Long ordemId, Long insumoId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico_insumo WHERE ordem_servico_id = ? AND insumo_id = ?",
                Integer.class, ordemId, insumoId);
        return count != null ? count : 0;
    }

    private Long criarOrdemAguardandoAprovacaoERetornarId(String tokenAtendente, String tokenMecanico,
                                                           Long peca1Id, int qtdPeca1,
                                                           Long peca2Id, int qtdPeca2,
                                                           Long insumo1Id, int qtdInsumo1,
                                                           Long insumo2Id, int qtdInsumo2) {
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", qtdPeca1))
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + peca1Id)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", qtdPeca2))
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + peca2Id)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", qtdInsumo1))
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumo1Id)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", qtdInsumo2))
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumo2Id)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao")
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/envio/" + ordemId)
                .then().statusCode(204);

        return ordemId;
    }

    @Test
    void shouldRecusarOrcamentoSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();

        Long peca1Id = 11L;
        Long peca2Id = 12L;
        Long insumo1Id = 21L;
        Long insumo2Id = 22L;
        stubCatalogoPeca(peca1Id, "45.00");
        stubCatalogoPeca(peca2Id, "60.00");
        stubCatalogoInsumo(insumo1Id, "35.00");
        stubCatalogoInsumo(insumo2Id, "20.00");

        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(
                tokenAtendente, tokenMecanico,
                peca1Id, 3, peca2Id, 2,
                insumo1Id, 4, insumo2Id, 1);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/recusar/" + ordemId)
                .then().statusCode(204);

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.CANCELADA, ordem.getStatus());
        Assertions.assertNotNull(ordem.getDataCancelamento());

        estoque.verify(exactly(1), postRequestedFor(urlEqualTo("/reserva/ordem-servico/" + ordemId + "/liberar")));
        estoque.verify(exactly(0), postRequestedFor(urlPathMatching(CONFIRMAR)));

        Assertions.assertEquals(1, contarVinculosPecaNoBanco(ordemId, peca1Id));
        Assertions.assertEquals(1, contarVinculosPecaNoBanco(ordemId, peca2Id));
        Assertions.assertEquals(1, contarVinculosInsumoNoBanco(ordemId, insumo1Id));
        Assertions.assertEquals(1, contarVinculosInsumoNoBanco(ordemId, insumo2Id));
    }
}
