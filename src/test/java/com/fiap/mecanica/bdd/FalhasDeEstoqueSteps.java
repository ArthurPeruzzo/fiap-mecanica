package com.fiap.mecanica.bdd;

import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OutboxEventoRepository;
import com.fiap.mecanica.resources.EstoqueSimulado;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Passos dos cenários de falha na integração com o estoque. O estado do cenário (tokens, ids, OS) vem de
 * {@link OrdemDeServicoSteps}, injetada aqui: as classes de cola são beans do cenário e podem se referenciar.
 */
public class FalhasDeEstoqueSteps {

    @Autowired private OrdemDeServicoSteps ordemDeServico;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private OutboxEventoRepository outboxEventoRepository;

    private final EstoqueSimulado estoque = new EstoqueSimulado(CucumberSpringConfig.estoqueSimulado());

    private Response ultimaResposta;

    // ------------------------------------------------------- falhas do estoque

    @Dado("que o estoque não tem saldo suficiente para a reserva")
    public void queOEstoqueNaoTemSaldo() {
        estoque.stubReservaStatus(422);
    }

    @Dado("que o estoque responde com erro interno às reservas")
    public void queOEstoqueRespondeComErroInterno() {
        estoque.stubReservaStatus(500);
        estoque.stubConfirmarELiberarOk();
    }

    @Dado("que o estoque demora mais do que o limite de espera para responder à reserva")
    public void queOEstoqueDemora() {
        // O perfil de teste limita a espera a 1 s; a resposta só chega depois de 2 s.
        estoque.stubReservaLenta(2000);
        estoque.stubConfirmarELiberarOk();
    }

    @Dado("que o estoque está fora do ar")
    public void queOEstoqueEstaForaDoAr() {
        estoque.stubForaDoAr();
    }

    // ----------------------------------------------------------------- ações

    @Quando("o atendente tenta abrir uma OS com {int} unidades da peça {long} e {int} do insumo {long}")
    public void oAtendenteTentaAbrirUmaOS(int qtdPeca, Long pecaId, int qtdInsumo, Long insumoId) {
        String corpo = String.format("""
                {"clienteId":%d,"veiculoId":%d,"descricao":"Barulho ao frear","servicosIds":[%d],
                 "pecas":[{"id":%d,"quantidade":%d}],"insumos":[{"id":%d,"quantidade":%d}]}""",
                ordemDeServico.clienteId(), ordemDeServico.veiculoId(), ordemDeServico.servicoId(),
                pecaId, qtdPeca, insumoId, qtdInsumo);
        ultimaResposta = RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + ordemDeServico.tokenAtendente())
                .body(corpo)
                .when().post("/ordem-servico");
    }

    @Dado("que uma OS está em diagnóstico com o mecânico responsável")
    public void queUmaOSEstaEmDiagnostico() {
        String corpo = String.format("{\"clienteId\":%d,\"veiculoId\":%d,\"descricao\":\"Barulho ao frear\"}",
                ordemDeServico.clienteId(), ordemDeServico.veiculoId());
        Long ordemId = RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + ordemDeServico.tokenAtendente())
                .body(corpo)
                .when().post("/ordem-servico")
                .then().statusCode(201)
                .extract().as(Long.class);
        ordemDeServico.definirOrdem(ordemId);
        RestAssured.given().header("Authorization", "Bearer " + ordemDeServico.tokenMecanico())
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);
    }

    @Quando("o mecânico tenta vincular {int} unidades da peça {long} à OS")
    public void oMecanicoTentaVincularAPeca(int quantidade, Long pecaId) {
        ultimaResposta = RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + ordemDeServico.tokenMecanico())
                .body("{\"quantidade\":" + quantidade + "}")
                .when().put("/ordem-servico/" + ordemDeServico.ordemId() + "/pecas/" + pecaId);
    }

    @Quando("o atendente recusa o orçamento")
    public void oAtendenteRecusaOOrcamento() {
        RestAssured.given().header("Authorization", "Bearer " + ordemDeServico.tokenAtendente())
                .when().post("/ordem-servico/orcamento/recusar/" + ordemDeServico.ordemId())
                .then().statusCode(204);
    }

    // ---------------------------------------------------------- verificações

    @Então("o pedido é recusado com o status {int} e a mensagem {string}")
    public void oPedidoERecusadoComMensagem(int status, String mensagem) {
        assertThat(ultimaResposta.statusCode()).isEqualTo(status);
        assertThat(ultimaResposta.jsonPath().getString("message")).isEqualTo(mensagem);
    }

    @Então("o pedido é recusado com o status {int}")
    public void oPedidoERecusado(int status) {
        assertThat(ultimaResposta.statusCode()).isEqualTo(status);
    }

    @Então("nenhuma OS foi gravada")
    public void nenhumaOSFoiGravada() {
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ordem_servico", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT (SELECT COUNT(*) FROM ordem_servico_peca) + (SELECT COUNT(*) FROM ordem_servico_insumo)",
                Integer.class)).isZero();
    }

    @Então("o estoque foi instruído a liberar as reservas que possam ter sido feitas")
    public void oEstoqueFoiInstruidoALiberar() {
        // A resposta se perdeu, mas o estoque pode ter reservado: o monólito compensa pedindo a liberação.
        assertThat(estoque.totalPedidosDeLiberacao()).isEqualTo(1);
    }

    @Então("a OS não tem a peça {long} vinculada")
    public void aOSNaoTemAPecaVinculada(Long pecaId) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico_peca WHERE ordem_servico_id = ? AND peca_id = ?",
                Integer.class, ordemDeServico.ordemId(), pecaId)).isZero();
    }

    @Então("a liberação das reservas da OS ficou agendada para quando o estoque voltar")
    public void aLiberacaoFicouAgendada() {
        // O comando fica na outbox (o relay está desligado nos testes) e é entregue quando houver broker e estoque.
        var pendentes = outboxEventoRepository.findAll().stream()
                .filter(e -> "LIBERAR_RESERVA".equals(e.getTipo()))
                .filter(e -> ordemDeServico.ordemId().equals(e.getAgregadoId()))
                .filter(e -> e.getEnviadoEm() == null)
                .toList();
        assertThat(pendentes).hasSize(1);
    }
}
