package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.infra.gateway.repository.AtendenteRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.MecanicoRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.VeiculoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.LinkAprovacaoOrcamentoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.ServicoRepository;
import com.fiap.mecanica.resources.EstoqueSimulado;
import com.fiap.mecanica.resources.MassaDeTestes;
import com.fiap.mecanica.resources.testcontainer.AbstractContainer;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.infra.config.SecurityConfiguration;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.RoleRepository;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

/**
 * Base dos testes de integração da ordem de serviço. A massa de dados ({@link MassaDeTestes}) e o fiap-estoque simulado
 * ({@link EstoqueSimulado}) vivem em classes públicas, compartilhadas com o BDD (Cucumber); aqui só ficam os atalhos que
 * os testes JUnit já usavam, com as mesmas assinaturas.
 */
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractOrdemDeServicoIntegrationTest extends AbstractContainer {

    @Autowired protected UserRepository userRepository;
    @Autowired protected RoleRepository roleRepository;
    @Autowired protected SecurityConfiguration securityConfiguration;
    @Autowired protected AtendenteRepository atendenteRepository;
    @Autowired protected MecanicoRepository mecanicoRepository;
    @Autowired protected ClienteRepository clienteRepository;
    @Autowired protected VeiculoRepository veiculoRepository;
    @Autowired protected OrdemDeServicoRepository ordemDeServicoRepository;
    @Autowired protected LinkAprovacaoOrcamentoRepository linkAprovacaoOrcamentoRepository;
    @Autowired protected ServicoRepository servicoRepository;
    @Autowired protected JdbcTemplate jdbcTemplate;

    /** Ids do catálogo do fiap-estoque simulado: o monólito não guarda peça nem insumo. */
    protected static final Long PECA_ID = EstoqueSimulado.PECA_ID;
    protected static final Long INSUMO_ID = EstoqueSimulado.INSUMO_ID;
    protected static final String RESERVAR_ITENS = EstoqueSimulado.RESERVAR_ITENS;
    protected static final String CONFIRMAR = EstoqueSimulado.CONFIRMAR;
    protected static final String LIBERAR = EstoqueSimulado.LIBERAR;

    private EstoqueSimulado estoqueSimulado() {
        return new EstoqueSimulado(estoque);
    }

    private MassaDeTestes massa() {
        return new MassaDeTestes(userRepository, roleRepository, securityConfiguration, atendenteRepository,
                mecanicoRepository, clienteRepository, veiculoRepository, servicoRepository);
    }

    protected void stubCatalogo() {
        estoqueSimulado().stubCatalogo();
    }

    protected void stubCatalogoVazio() {
        estoqueSimulado().stubCatalogoVazio();
    }

    protected void stubReservaOk() {
        estoqueSimulado().stubReservaOk();
    }

    protected void stubReservaStatus(int status) {
        estoqueSimulado().stubReservaStatus(status);
    }

    protected void stubConfirmarEliberarOk() {
        estoqueSimulado().stubConfirmarELiberarOk();
    }

    protected void stubCatalogoPeca(Long id, String preco) {
        estoqueSimulado().stubCatalogoPeca(id, preco);
    }

    protected void stubCatalogoInsumo(Long id, String preco) {
        estoqueSimulado().stubCatalogoInsumo(id, preco);
    }

    /** As quantidadeTotal enviadas ao estoque nas reservas, na ordem em que foram feitas. */
    protected List<Integer> quantidadesReservadasNoEstoque() {
        return estoqueSimulado().quantidadesReservadas();
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        // Respostas padrão do fiap-estoque; cada teste sobrescreve só o que quer variar.
        stubCatalogo();
        stubReservaOk();
        stubConfirmarEliberarOk();
    }

    protected String obterToken(RoleEnum role, String cpf) {
        return massa().obterToken(role, cpf);
    }

    protected String obterTokenAtendente() {
        return massa().obterTokenAtendente();
    }

    protected String obterTokenMecanico() {
        return massa().obterTokenMecanico();
    }

    protected String obterTokenOutroMecanico() {
        return massa().obterTokenOutroMecanico();
    }

    protected Long criarClienteERetornarId() {
        return massa().criarClienteERetornarId();
    }

    protected Long criarVeiculoERetornarId(Long clienteId) {
        return massa().criarVeiculoERetornarId(clienteId);
    }

    protected Long criarOrdemERetornarId(String token, Long clienteId, Long veiculoId) {
        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"veiculoId\":%d,\"descricao\":\"Barulho ao frear\"}", clienteId, veiculoId))
                .when().post("/ordem-servico")
                .then().statusCode(201);
        return ordemDeServicoRepository.findAll().getFirst().getId();
    }

    protected Long criarOrdemEmDiagnosticoERetornarId(String tokenAtendente, String tokenMecanico) {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        return ordemId;
    }

    protected Long criarOrdemAguardandoAprovacaoERetornarId(String tokenAtendente, String tokenMecanico) {
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);
        Long servicoId = criarServicoERetornarId();

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

    protected Long criarOrdemAguardandoAprovacaoComPecaEInsumoERetornarId(String tokenAtendente, String tokenMecanico,
                                                                           Long pecaId, int qtdPeca,
                                                                           Long insumoId, int qtdInsumo) {
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(tokenAtendente, tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", qtdPeca))
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body(Map.of("quantidade", qtdInsumo))
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
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

    protected Long criarServicoERetornarId() {
        return massa().criarServicoERetornarId();
    }
}
