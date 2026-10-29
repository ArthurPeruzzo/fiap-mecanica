package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@TestPropertySource(properties = "estoque.fake=false")
class OrcamentoAprovadoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    @BeforeEach
    void stubsDoEstoque() {
        stubCatalogo();
        stubReservaOk();
        stubConfirmarEliberarOk();
    }

    @Test
    void shouldAprovarOrcamentoSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/aprovar/" + ordemId)
                .then().statusCode(204);

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.EM_EXECUCAO, ordem.getStatus());
        Assertions.assertNotNull(ordem.getDataAprovacao());
    }

    @Test
    void shouldConfirmarReservasNoEstoqueAoAprovar() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long pecaId = PECA_ID;
        Long insumoId = INSUMO_ID;
        Long ordemId = criarOrdemAguardandoAprovacaoComPecaEInsumoERetornarId(
                tokenAtendente, tokenMecanico, pecaId, 3, insumoId, 2);

        estoque.verify(exactly(0), postRequestedFor(urlPathMatching(CONFIRMAR)));

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/aprovar/" + ordemId)
                .then().statusCode(204);

        estoque.verify(exactly(1), postRequestedFor(urlEqualTo("/reserva/ordem-servico/" + ordemId + "/confirmar")));
        estoque.verify(exactly(0), postRequestedFor(urlPathMatching(LIBERAR)));
    }
}
