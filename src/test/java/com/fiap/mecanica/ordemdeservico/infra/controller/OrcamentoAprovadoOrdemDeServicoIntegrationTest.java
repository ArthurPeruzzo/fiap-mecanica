package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

class OrcamentoAprovadoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

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
        Assertions.assertEquals(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO, ordem.getStatus());
        Assertions.assertNotNull(ordem.getDataAprovacao());
    }

    @Test
    void shouldSolicitarPagamentoPelaOutboxENaoConfirmarReservasAoAprovar() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long pecaId = PECA_ID;
        Long insumoId = INSUMO_ID;
        Long ordemId = criarOrdemAguardandoAprovacaoComPecaEInsumoERetornarId(
                tokenAtendente, tokenMecanico, pecaId, 3, insumoId, 2);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/aprovar/" + ordemId)
                .then().statusCode(204);

        // A baixa só acontece depois do pagamento: nenhuma chamada ao estoque na aprovação.
        estoque.verify(exactly(0), postRequestedFor(urlPathMatching(CONFIRMAR)));
        estoque.verify(exactly(0), postRequestedFor(urlPathMatching(LIBERAR)));
        // O pedido de cobrança fica na outbox, na mesma transação que gravou o novo status.
        var evento = jdbcTemplate.queryForMap(
                "SELECT tipo, exchange, routing_key, payload FROM outbox_evento WHERE agregado_id = ?", ordemId);
        Assertions.assertEquals("SOLICITAR_PAGAMENTO", evento.get("tipo"));
        Assertions.assertEquals("cobranca.comandos", evento.get("exchange"));
        Assertions.assertEquals("pagamento.solicitar", evento.get("routing_key"));
        Assertions.assertTrue(evento.get("payload").toString().contains("\"ordemServicoId\":" + ordemId));
    }
}
