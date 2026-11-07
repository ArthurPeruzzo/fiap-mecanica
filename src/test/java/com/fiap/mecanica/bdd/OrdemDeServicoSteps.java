package com.fiap.mecanica.bdd;

import com.fiap.mecanica.gestao.infra.gateway.repository.AtendenteRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.MecanicoRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.VeiculoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.ServicoRepository;
import com.fiap.mecanica.resources.EstoqueSimulado;
import com.fiap.mecanica.resources.MassaDeTestes;
import com.fiap.mecanica.shared.seguranca.infra.config.SecurityConfiguration;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.RoleRepository;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.UserRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Passos do ciclo de vida da OS. Falam com a aplicação só pela API HTTP (como um cliente real) e conferem o estoque pelo
 * que o monólito PEDIU a ele (o fiap-estoque é simulado). A massa de dados e o estoque simulado são os mesmos dos testes
 * de integração JUnit ({@link MassaDeTestes} e {@link EstoqueSimulado}).
 */
public class OrdemDeServicoSteps {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SecurityConfiguration securityConfiguration;
    @Autowired private AtendenteRepository atendenteRepository;
    @Autowired private MecanicoRepository mecanicoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private final EstoqueSimulado estoque = new EstoqueSimulado(CucumberSpringConfig.estoqueSimulado());

    private MassaDeTestes massa;
    private String tokenAtendente;
    private String tokenMecanico;
    private Long clienteId;
    private Long veiculoId;
    private Long servicoId;
    private Long ordemId;

    private MassaDeTestes massa() {
        if (massa == null) {
            massa = new MassaDeTestes(userRepository, roleRepository, securityConfiguration, atendenteRepository,
                    mecanicoRepository, clienteRepository, veiculoRepository, servicoRepository);
        }
        return massa;
    }

    // ---------------------------------------------------------------- contexto

    @Dado("que o estoque tem a peça {long} por {int} reais e o insumo {long} por {int} reais")
    public void queOEstoqueTem(Long pecaId, int precoPeca, Long insumoId, int precoInsumo) {
        estoque.stubCatalogoPeca(pecaId, precoPeca + ".00");
        estoque.stubCatalogoInsumo(insumoId, precoInsumo + ".00");
        estoque.stubReservaOk();
        estoque.stubConfirmarELiberarOk();
    }

    @Dado("que a oficina oferece o serviço de troca de óleo por {int} reais")
    public void queAOficinaOfereceOServico(int preco) {
        // O serviço da massa de testes custa R$ 150,00, o valor combinado no cenário.
        assertThat(preco).as("preço do serviço da massa de testes").isEqualTo(150);
        servicoId = massa().criarServicoERetornarId();
    }

    @Dado("que a oficina tem um atendente e um mecânico")
    public void queAOficinaTemAtendenteEMecanico() {
        tokenAtendente = massa().obterTokenAtendente();
        tokenMecanico = massa().obterTokenMecanico();
    }

    @Dado("que existe um cliente com um veículo cadastrado")
    public void queExisteUmClienteComVeiculo() {
        clienteId = massa().criarClienteERetornarId();
        veiculoId = massa().criarVeiculoERetornarId(clienteId);
    }

    // ------------------------------------------------------------------- ações

    @Dado("que o atendente abre uma OS com {int} unidades da peça {long} e {int} do insumo {long}")
    public void oAtendenteAbreUmaOS(int qtdPeca, Long pecaId, int qtdInsumo, Long insumoId) {
        String corpo = String.format("""
                {"clienteId":%d,"veiculoId":%d,"descricao":"Barulho ao frear","servicosIds":[%d],
                 "pecas":[{"id":%d,"quantidade":%d}],"insumos":[{"id":%d,"quantidade":%d}]}""",
                clienteId, veiculoId, servicoId, pecaId, qtdPeca, insumoId, qtdInsumo);
        ordemId = RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenAtendente)
                .body(corpo)
                .when().post("/ordem-servico")
                .then().statusCode(201)
                .extract().as(Long.class);
    }

    @Quando("o mecânico inicia e conclui o diagnóstico")
    public void oMecanicoIniciaEConcluiODiagnostico() {
        patch("/ordem-servico/" + ordemId + "/diagnostico", tokenMecanico);
        patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao", tokenMecanico);
    }

    @Quando("o atendente envia o orçamento ao cliente")
    public void oAtendenteEnviaOOrcamento() {
        post("/ordem-servico/orcamento/envio/" + ordemId, tokenAtendente);
    }

    @Quando("o atendente aprova o orçamento")
    public void oAtendenteAprovaOOrcamento() {
        post("/ordem-servico/orcamento/aprovar/" + ordemId, tokenAtendente);
    }

    @Quando("o mecânico executa e finaliza o serviço")
    public void oMecanicoExecutaEFinalizaOServico() {
        patch("/ordem-servico/" + ordemId + "/servicos/" + servicoId + "/iniciar", tokenMecanico);
        patch("/ordem-servico/" + ordemId + "/servicos/" + servicoId + "/finalizar", tokenMecanico);
    }

    @Quando("o atendente entrega o veículo")
    public void oAtendenteEntregaOVeiculo() {
        patch("/ordem-servico/" + ordemId + "/entregar", tokenAtendente);
    }

    // ------------------------------------------------------------ verificações

    @Então("a OS está {string}")
    public void aOSEsta(String statusEsperado) {
        String status = RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().get("/ordem-servico/" + ordemId + "/status")
                .then().statusCode(200)
                .extract().path("status");
        assertThat(status).isEqualTo(statusEsperado);
    }

    @Então("o estoque reservou {int} unidades da peça {long} e {int} do insumo {long} para a OS")
    public void oEstoqueReservou(int qtdPeca, Long pecaId, int qtdInsumo, Long insumoId) {
        List<Map<String, Object>> itens = estoque.itensDaUltimaReserva(ordemId);
        assertThat(itens).extracting(i -> i.get("tipoItem") + ":" + i.get("itemId") + ":" + i.get("quantidadeTotal"))
                .containsExactlyInAnyOrder("PECA:" + pecaId + ":" + qtdPeca, "INSUMO:" + insumoId + ":" + qtdInsumo);
    }

    @Então("o orçamento é de {int} reais")
    public void oOrcamentoE(int valor) {
        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT orcamento_total FROM ordem_servico WHERE id = ?", BigDecimal.class, ordemId);
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(valor));
    }

    @Então("o estoque ainda não recebeu o pedido de baixa das reservas")
    public void oEstoqueAindaNaoRecebeuABaixa() {
        assertThat(estoque.pedidosDeConfirmacao(ordemId)).isZero();
    }

    @Então("o estoque foi instruído a confirmar a baixa das reservas da OS")
    public void oEstoqueFoiInstruidoAConfirmar() {
        assertThat(estoque.pedidosDeConfirmacao(ordemId)).isEqualTo(1);
    }

    @Então("o estoque não recebeu nenhum pedido de liberação das reservas")
    public void oEstoqueNaoRecebeuLiberacao() {
        assertThat(estoque.pedidosDeLiberacao(ordemId)).isZero();
    }

    // Estado do cenário compartilhado com os passos de falha (as classes de cola são beans do cenário e se injetam).
    Long ordemId() { return ordemId; }
    void definirOrdem(Long id) { this.ordemId = id; }
    String tokenAtendente() { return tokenAtendente; }
    String tokenMecanico() { return tokenMecanico; }
    Long clienteId() { return clienteId; }
    Long veiculoId() { return veiculoId; }
    Long servicoId() { return servicoId; }

    private void patch(String caminho, String token) {
        RestAssured.given().header("Authorization", "Bearer " + token).when().patch(caminho).then().statusCode(204);
    }

    private void post(String caminho, String token) {
        RestAssured.given().header("Authorization", "Bearer " + token).when().post(caminho).then().statusCode(204);
    }
}
