package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OutboxEventoRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

/**
 * Recusar o orçamento grava o comando de liberação na outbox junto com o contexto de trace da requisição, para o
 * consumo no estoque continuar o MESMO trace (o relay publica depois, em outra thread).
 */
@TestPropertySource(properties = "management.tracing.sampling.probability=1.0")
class PropagacaoDeTraceOutboxIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private static final String TRACE_ID = "0af7651916cd43dd8448eb211c80319c";

    @Autowired
    private OutboxEventoRepository outboxEventoRepository;

    @Test
    void shouldGuardarNaOutboxOTraceDaRequisicaoDeRecusa() {
        String tokenAtendente = obterTokenAtendente();
        Long ordemId = criarOrdemAguardandoAprovacaoERetornarId(tokenAtendente, obterTokenMecanico());

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .header("traceparent", "00-" + TRACE_ID + "-b7ad6b7169203331-01")
                .when().post("/ordem-servico/orcamento/recusar/" + ordemId)
                .then().statusCode(204);

        var comandos = outboxEventoRepository.findAll();
        Assertions.assertEquals(1, comandos.size());
        String traceparent = comandos.getFirst().getTraceparent();
        Assertions.assertNotNull(traceparent, "o comando deveria guardar o contexto de trace da requisição");
        Assertions.assertEquals(TRACE_ID, traceparent.split("-")[1],
                "o trace gravado é o da requisição, que continuou o trace recebido no cabeçalho");
    }
}
