package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class DetalhamentoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    @Test
    void shouldReturnDetalhamentoOrdemDeServicoSuccessfully() {
        String tokenAtendente = obterTokenAtendente();
        String tokenAdmin = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "77722244432");

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
                .body("content[0].nomeCliente", Matchers.equalTo("Maria"))
                .body("content[0].veiculo", Matchers.equalTo("Civic 2020 ABC-1234"))
                .body("content[0].nomeAtendente", Matchers.equalTo("João Silva"))
                .body("content[0].nomeMecanico", Matchers.nullValue())
                .body("content[0].status", Matchers.equalTo("RECEBIDA"))
                .body("content[0].descricao", Matchers.equalTo("Barulho ao frear"))
                .body("content[0].servicos.size()", Matchers.equalTo(0))
                .body("content[0].pecas.size()", Matchers.equalTo(0))
                .body("content[0].insumos.size()", Matchers.equalTo(0))
                .body("content[0].tempoMedioExecucaoServicos", Matchers.nullValue())
                .body("totalElements", Matchers.equalTo(1))
                .body("totalPages", Matchers.equalTo(1));
    }

    @Test
    void shouldReturnNullTempoMedioWhenSemServicosFinalizados() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        String tokenAdmin = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "77722244432");

        Long s1 = criarServicoERetornarId();
        Long s2 = criarServicoERetornarId();
        Long ordemId = criarOrdemEmExecucaoComServicos(tokenAtendente, tokenMecanico, List.of(s1, s2));

        // s1 EM_EXECUCAO, s2 NAO_INICIADO — nenhum finalizado
        iniciarServico(ordemId, s1, tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/ordem-servico/detalhamento")
                .then().statusCode(200)
                .body("content[0].tempoMedioExecucaoServicos", Matchers.nullValue());
    }

    @Test
    void shouldReturnTempoMedioConsideringOnlyServicosFinalizados() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        String tokenAdmin = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "77722244432");

        Long s1 = criarServicoERetornarId();
        Long s2 = criarServicoERetornarId();
        Long s3 = criarServicoERetornarId();
        Long s4 = criarServicoERetornarId();
        Long ordemId = criarOrdemEmExecucaoComServicos(tokenAtendente, tokenMecanico, List.of(s1, s2, s3, s4));

        // finalizar s1 e s2 e sobrescrever timestamps: s1=2h, s2=4h → média=3h
        iniciarEFinalizarServico(ordemId, s1, tokenMecanico);
        iniciarEFinalizarServico(ordemId, s2, tokenMecanico);
        var base = LocalDateTime.of(2024, 1, 15, 8, 0);
        sobrescreverTimestampsServico(ordemId, s1, base, base.plusHours(2));
        sobrescreverTimestampsServico(ordemId, s2, base, base.plusHours(4));

        // s3 em execução, s4 não iniciado
        iniciarServico(ordemId, s3, tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/ordem-servico/detalhamento")
                .then().statusCode(200)
                .body("content[0].tempoMedioExecucaoServicos.dias", Matchers.equalTo(0))
                .body("content[0].tempoMedioExecucaoServicos.horas", Matchers.equalTo(3))
                .body("content[0].tempoMedioExecucaoServicos.minutos", Matchers.equalTo(0));
    }

    @Test
    void shouldReturnTempoMedioSpanningDays() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        String tokenAdmin = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "77722244432");

        Long s1 = criarServicoERetornarId();
        Long s2 = criarServicoERetornarId();
        Long ordemId = criarOrdemEmExecucaoComServicos(tokenAtendente, tokenMecanico, List.of(s1, s2));

        // finalizar um único serviço
        iniciarEFinalizarServico(ordemId, s1, tokenMecanico);
        // 1d 4h 30min
        var inicio = LocalDateTime.of(2024, 1, 15, 9, 0);
        var fim = LocalDateTime.of(2024, 1, 16, 13, 30);
        sobrescreverTimestampsServico(ordemId, s1, inicio, fim);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAdmin)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/ordem-servico/detalhamento")
                .then().statusCode(200)
                .body("content[0].tempoMedioExecucaoServicos.dias", Matchers.equalTo(1))
                .body("content[0].tempoMedioExecucaoServicos.horas", Matchers.equalTo(4))
                .body("content[0].tempoMedioExecucaoServicos.minutos", Matchers.equalTo(30));
    }

    private Long criarOrdemEmExecucaoComServicos(String tokenAtendente, String tokenMecanico, List<Long> servicoIds) {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);

        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        for (Long servicoId : servicoIds) {
            RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                    .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                    .then().statusCode(204);
        }

        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao")
                .then().statusCode(204);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/envio/" + ordemId)
                .then().statusCode(204);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/aprovar/" + ordemId)
                .then().statusCode(204);

        return ordemId;
    }

    private void iniciarServico(Long ordemId, Long servicoId, String tokenMecanico) {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/servicos/" + servicoId + "/iniciar")
                .then().statusCode(204);
    }

    private void iniciarEFinalizarServico(Long ordemId, Long servicoId, String tokenMecanico) {
        iniciarServico(ordemId, servicoId, tokenMecanico);
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/servicos/" + servicoId + "/finalizar")
                .then().statusCode(204);
    }

    private void sobrescreverTimestampsServico(Long ordemId, Long servicoId, LocalDateTime inicio, LocalDateTime fim) {
        jdbcTemplate.update(
                "UPDATE ordem_servico_servico SET data_inicio_execucao = ?, data_fim_execucao = ? WHERE ordem_servico_id = ? AND servico_id = ?",
                inicio, fim, ordemId, servicoId);
    }
}
