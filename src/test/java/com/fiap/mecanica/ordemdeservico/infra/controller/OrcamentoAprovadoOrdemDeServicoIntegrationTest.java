package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class OrcamentoAprovadoOrdemDeServicoIntegrationTest extends AbstractOrdemDeServicoIntegrationTest {

    private Integer obterEstoqueNoBanco(String tabela, Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade_estoque FROM " + tabela + " WHERE id = ?", Integer.class, id);
    }

    private Integer obterReservadaNoBanco(String tabela, Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade_reservada FROM " + tabela + " WHERE id = ?", Integer.class, id);
    }

    private List<String> obterStatusReservasNoBanco(Long ordemId) {
        return jdbcTemplate.queryForList(
                "SELECT status FROM reserva_estoque WHERE ordem_servico_id = ?", String.class, ordemId);
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
    void shouldBaixarEstoqueEConfirmarReservasAoAprovar() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long pecaId = criarPecaERetornarId(10);
        Long insumoId = criarInsumoERetornarId(10);
        Long ordemId = criarOrdemAguardandoAprovacaoComPecaEInsumoERetornarId(
                tokenAtendente, tokenMecanico, pecaId, 3, insumoId, 2);

        Assertions.assertEquals(10, obterEstoqueNoBanco("peca", pecaId));
        Assertions.assertEquals(3, obterReservadaNoBanco("peca", pecaId));

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/aprovar/" + ordemId)
                .then().statusCode(204);

        Assertions.assertEquals(7, obterEstoqueNoBanco("peca", pecaId));
        Assertions.assertEquals(0, obterReservadaNoBanco("peca", pecaId));
        Assertions.assertEquals(8, obterEstoqueNoBanco("insumo", insumoId));
        Assertions.assertEquals(0, obterReservadaNoBanco("insumo", insumoId));
        Assertions.assertEquals(List.of("CONFIRMADA", "CONFIRMADA"), obterStatusReservasNoBanco(ordemId));
    }
}
