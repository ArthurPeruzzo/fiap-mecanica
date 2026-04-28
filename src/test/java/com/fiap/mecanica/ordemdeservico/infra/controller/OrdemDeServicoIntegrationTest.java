package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.core.domain.Turno;
import com.fiap.mecanica.gestao.infra.gateway.entity.AtendenteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.AtendenteRepository;
import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.gestao.infra.gateway.entity.MecanicoEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.MecanicoRepository;
import com.fiap.mecanica.gestao.infra.gateway.entity.VeiculoEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.VeiculoRepository;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.infra.gateway.entity.InsumoEntity;
import com.fiap.mecanica.estoque.infra.gateway.entity.PecaEntity;
import com.fiap.mecanica.estoque.infra.gateway.repository.InsumoRepository;
import com.fiap.mecanica.estoque.infra.gateway.repository.PecaRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.ServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoPecaRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.ServicoRepository;
import com.fiap.mecanica.resources.testcontainer.AbstractContainer;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.infra.config.SecurityConfiguration;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.RoleEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.UserEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.RoleRepository;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.UserRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrdemDeServicoIntegrationTest extends AbstractContainer {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SecurityConfiguration securityConfiguration;
    @Autowired private AtendenteRepository atendenteRepository;
    @Autowired private MecanicoRepository mecanicoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private OrdemDeServicoRepository ordemDeServicoRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private PecaRepository pecaRepository;
    @Autowired private InsumoRepository insumoRepository;
    @Autowired private OrdemDeServicoPecaRepository ordemDeServicoPecaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    private String obterToken(RoleEnum role, String email) {
        List<RoleEntity> roles = roleRepository.findByNameIn(List.of(role));
        userRepository.saveAndFlush(UserEntity.builder()
                .email(email)
                .password(securityConfiguration.passwordEncoder().encode("senha123"))
                .roles(roles)
                .build());
        return RestAssured
                .given().contentType("application/json")
                .body(String.format("{\"email\":\"%s\",\"password\":\"senha123\"}", email))
                .when().post("/authenticate/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    private String obterTokenAtendente() {
        String token = obterToken(RoleEnum.ROLE_ATENDENTE, "atendente@test.com");
        UserEntity user = userRepository.findByEmail("atendente@test.com").orElseThrow();
        atendenteRepository.saveAndFlush(AtendenteEntity.builder()
                .nome("João").sobrenome("Silva").turno(Turno.INTEGRAL).userId(user.getId()).build());
        return token;
    }

    private String obterTokenMecanico() {
        String token = obterToken(RoleEnum.ROLE_MECANICO, "mecanico@test.com");
        UserEntity user = userRepository.findByEmail("mecanico@test.com").orElseThrow();
        mecanicoRepository.saveAndFlush(MecanicoEntity.builder()
                .nome("Carlos").sobrenome("Lima").especialidade("Motor").userId(user.getId()).build());
        return token;
    }

    private String obterTokenOutroMecanico() {
        String token = obterToken(RoleEnum.ROLE_MECANICO, "mecanico2@test.com");
        UserEntity user = userRepository.findByEmail("mecanico2@test.com").orElseThrow();
        mecanicoRepository.saveAndFlush(MecanicoEntity.builder()
                .nome("Pedro").sobrenome("Costa").especialidade("Freios").userId(user.getId()).build());
        return token;
    }

    private Long criarClienteERetornarId() {
        return clienteRepository.saveAndFlush(ClienteEntity.builder()
                .nome("Maria").sobrenome("Santos").cpf("12345678900").build()).getId();
    }

    private Long criarVeiculoERetornarId(Long clienteId) {
        return veiculoRepository.saveAndFlush(VeiculoEntity.builder()
                .placa("ABC1234").modelo("Civic").ano(2020)
                .cliente(ClienteEntity.builder().id(clienteId).build()).build()).getId();
    }

    private Long criarOrdemERetornarId(String token, Long clienteId, Long veiculoId) {
        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"veiculoId\":%d,\"descricao\":\"Barulho ao frear\"}", clienteId, veiculoId))
                .when().post("/ordem-servico")
                .then().statusCode(201);
        return ordemDeServicoRepository.findAll().getFirst().getId();
    }

    private Long criarServicoERetornarId() {
        return servicoRepository.saveAndFlush(ServicoEntity.builder()
                .nome("Troca de óleo")
                .descricao("Serviço de troca de óleo do motor")
                .preco(new BigDecimal("150.00"))
                .build()).getId();
    }

    private Long criarPecaERetornarId(Integer quantidadeEstoque) {
        return pecaRepository.saveAndFlush(PecaEntity.builder()
                .nome("Filtro de óleo")
                .descricao("Filtro original")
                .preco(new BigDecimal("45.00"))
                .quantidadeEstoque(quantidadeEstoque)
                .build()).getId();
    }

    private int contarVinculosPecaNoBanco(Long ordemId, Long pecaId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico_peca WHERE ordem_servico_id = ? AND peca_id = ?",
                Integer.class, ordemId, pecaId);
        return count != null ? count : 0;
    }

    private Integer obterQuantidadePecaNoBanco(Long ordemId, Long pecaId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade FROM ordem_servico_peca WHERE ordem_servico_id = ? AND peca_id = ?",
                Integer.class, ordemId, pecaId);
    }

    private Long criarInsumoERetornarId(Integer quantidadeEstoque) {
        return insumoRepository.saveAndFlush(InsumoEntity.builder()
                .nome("Óleo motor 5W30")
                .descricao("Óleo sintético")
                .preco(new BigDecimal("35.00"))
                .quantidadeEstoque(quantidadeEstoque)
                .unidadeMedida(UnidadeMedida.LITRO)
                .build()).getId();
    }

    private int contarVinculosInsumoNoBanco(Long ordemId, Long insumoId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico_insumo WHERE ordem_servico_id = ? AND insumo_id = ?",
                Integer.class, ordemId, insumoId);
        return count != null ? count : 0;
    }

    private Integer obterQuantidadeInsumoNoBanco(Long ordemId, Long insumoId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade FROM ordem_servico_insumo WHERE ordem_servico_id = ? AND insumo_id = ?",
                Integer.class, ordemId, insumoId);
    }

    private Long criarOrdemEmDiagnosticoERetornarId(String tokenAtendente, String tokenMecanico) {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        return ordemId;
    }

    private int contarVinculosNoBanco(Long ordemId, Long servicoId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico_servico WHERE ordem_servico_id = ? AND servico_id = ?",
                Integer.class, ordemId, servicoId);
        return count != null ? count : 0;
    }

    // --- criar ---

    @Test
    void shouldCreateOrdemDeServicoSuccessfully() {
        String token = obterTokenAtendente();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"veiculoId\":%d,\"descricao\":\"Barulho ao frear\"}", clienteId, veiculoId))
                .when().post("/ordem-servico")
                .then().statusCode(201);

        var ordens = ordemDeServicoRepository.findAll();
        Assertions.assertEquals(1, ordens.size());
        OrdemDeServicoEntity ordem = ordens.getFirst();
        Assertions.assertEquals(StatusOrdemDeServico.RECEBIDA, ordem.getStatus());
        Assertions.assertEquals(clienteId, ordem.getClienteId());
        Assertions.assertEquals(veiculoId, ordem.getVeiculoId());
        Assertions.assertEquals("Barulho ao frear", ordem.getDescricao());
        Assertions.assertNotNull(ordem.getDataCriacao());
        Assertions.assertNull(ordem.getDataInicioDiagnostico());
        Assertions.assertNull(ordem.getDataConclusaoDiagnostico());
    }

    // --- iniciarDiagnostico ---

    @Test
    void shouldIniciarDiagnosticoSuccessfully() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(obterTokenAtendente(), clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + obterTokenMecanico())
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.EM_DIAGNOSTICO, ordem.getStatus());
        Assertions.assertNotNull(ordem.getMecanicoId());
        Assertions.assertNotNull(ordem.getDataInicioDiagnostico());
        Assertions.assertNull(ordem.getDataConclusaoDiagnostico());
    }

    // --- concluirDiagnostico ---

    @Test
    void shouldConcluirDiagnosticoSuccessfully() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemERetornarId(obterTokenAtendente(), clienteId, veiculoId);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao")
                .then().statusCode(204);

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, ordem.getStatus());
        Assertions.assertNotNull(ordem.getDataInicioDiagnostico());
        Assertions.assertNotNull(ordem.getDataConclusaoDiagnostico());
    }

    @Test
    void shouldReturn422WhenSemServicosVinculadosOnConcluir() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemERetornarId(obterTokenAtendente(), clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao")
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível concluir o diagnóstico sem ao menos um serviço vinculado"));
    }

    @Test
    void shouldReturn422WhenMecanicoNaoEhResponsavelOnConcluir() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemERetornarId(obterTokenAtendente(), clienteId, veiculoId);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico")
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + obterTokenOutroMecanico())
                .when().patch("/ordem-servico/" + ordemId + "/diagnostico/conclusao")
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Somente o mecânico responsável pelo diagnóstico pode concluí-lo"));
    }

    // --- vincularServico ---

    @Test
    void shouldVincularServicoSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosNoBanco(ordemId, servicoId));
    }

    @Test
    void shouldReturn422WhenServicoJaVinculado() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Este serviço já está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaVincular() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincular() {
        String tokenMecanico = obterTokenMecanico();
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/9999/servicos/" + servicoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnVincular() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Serviço não encontrado"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnVincular() {
        RestAssured.given()
                .when().put("/ordem-servico/1/servicos/1")
                .then().statusCode(401);
    }

    // --- desvincularServico ---

    @Test
    void shouldDesvincularServicoSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(204);

        Assertions.assertEquals(0, contarVinculosNoBanco(ordemId, servicoId));
    }

    @Test
    void shouldReturn422WhenServicoNaoVinculado() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Este serviço não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaDesvincular() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/" + ordemId + "/servicos/" + servicoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível adicionar ou remover serviços se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincular() {
        String tokenMecanico = obterTokenMecanico();
        Long servicoId = criarServicoERetornarId();

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/9999/servicos/" + servicoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnDesvincular() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given()
                .header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/" + ordemId + "/servicos/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Serviço não encontrado"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnDesvincular() {
        RestAssured.given()
                .when().delete("/ordem-servico/1/servicos/1")
                .then().statusCode(401);
    }

    // --- vincularPeca ---

    @Test
    void shouldVincularPecaSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosPecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(3, obterQuantidadePecaNoBanco(ordemId, pecaId));
    }

    @Test
    void shouldSomarQuantidadeWhenPecaVinculadaJaExiste() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosPecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(5, obterQuantidadePecaNoBanco(ordemId, pecaId));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(1);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaVincularPeca() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível vincular peças se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/9999/pecas/" + pecaId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnVincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Peça não encontrada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNullOnVincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsZeroOnVincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":0}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser no mínimo 1"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnVincularPeca() {
        RestAssured.given().contentType("application/json")
                .body("{\"quantidade\":2}")
                .when().put("/ordem-servico/1/pecas/1")
                .then().statusCode(401);
    }

    // --- desvincularPeca ---

    @Test
    void shouldDesvincularPecaParcialmenteSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosPecaNoBanco(ordemId, pecaId));
        Assertions.assertEquals(3, obterQuantidadePecaNoBanco(ordemId, pecaId));
    }

    @Test
    void shouldDesvincularPecaIntegralmenteEDeletarVinculo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        Assertions.assertEquals(0, contarVinculosPecaNoBanco(ordemId, pecaId));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaDesvincularPeca() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível desvincular peças se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenPecaNaoVinculadaOnDesvincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Peça não está vinculada à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeMaiorQueVinculadaOnDesvincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":10}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Quantidade a desvincular é maior que a quantidade vinculada"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/9999/pecas/" + pecaId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnDesvincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Peça não encontrada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNullOnDesvincularPeca() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long pecaId = criarPecaERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().delete("/ordem-servico/" + ordemId + "/pecas/" + pecaId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnDesvincularPeca() {
        RestAssured.given().contentType("application/json")
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/1/pecas/1")
                .then().statusCode(401);
    }

    // --- vincularInsumo ---

    @Test
    void shouldVincularInsumoSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosInsumoNoBanco(ordemId, insumoId));
        Assertions.assertEquals(4, obterQuantidadeInsumoNoBanco(ordemId, insumoId));
    }

    @Test
    void shouldSomarQuantidadeWhenInsumoVinculadoJaExiste() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosInsumoNoBanco(ordemId, insumoId));
        Assertions.assertEquals(7, obterQuantidadeInsumoNoBanco(ordemId, insumoId));
    }

    @Test
    void shouldReturn422WhenEstoqueInsuficienteOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(1);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Estoque insuficiente para realizar a operação"));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaVincularInsumo() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível vincular insumos se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/9999/insumos/" + insumoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Insumo não encontrado"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNullOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsZeroOnVincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":0}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser no mínimo 1"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnVincularInsumo() {
        RestAssured.given().contentType("application/json")
                .body("{\"quantidade\":3}")
                .when().put("/ordem-servico/1/insumos/1")
                .then().statusCode(401);
    }

    // --- desvincularInsumo ---

    @Test
    void shouldDesvincularInsumoParcialmenteSuccessfully() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":5}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        Assertions.assertEquals(1, contarVinculosInsumoNoBanco(ordemId, insumoId));
        Assertions.assertEquals(3, obterQuantidadeInsumoNoBanco(ordemId, insumoId));
    }

    @Test
    void shouldDesvincularInsumoIntegralmenteEDeletarVinculo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        Assertions.assertEquals(0, contarVinculosInsumoNoBanco(ordemId, insumoId));
    }

    @Test
    void shouldReturn422WhenOrdemNaoEmDiagnosticoParaDesvincularInsumo() {
        String tokenAtendente = obterTokenAtendente();
        String tokenMecanico = obterTokenMecanico();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        Long ordemId = criarOrdemERetornarId(tokenAtendente, clienteId, veiculoId);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Não é possível desvincular insumos se a ordem de serviço não está em diagnóstico"));
    }

    @Test
    void shouldReturn422WhenInsumoNaoVinculadoOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Insumo não está vinculado à ordem de serviço"));
    }

    @Test
    void shouldReturn422WhenQuantidadeMaiorQueVinculadaOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":4}")
                .when().put("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":10}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Quantidade a desvincular é maior que a quantidade vinculada"));
    }

    @Test
    void shouldReturn404WhenOrdemNotFoundOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/9999/insumos/" + insumoId)
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Ordem de serviço não encontrada"));
    }

    @Test
    void shouldReturn404WhenInsumoNotFoundOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/9999")
                .then().statusCode(404)
                .body("message", Matchers.equalTo("Insumo não encontrado"));
    }

    @Test
    void shouldReturn400WhenQuantidadeIsNullOnDesvincularInsumo() {
        String tokenMecanico = obterTokenMecanico();
        Long ordemId = criarOrdemEmDiagnosticoERetornarId(obterTokenAtendente(), tokenMecanico);
        Long insumoId = criarInsumoERetornarId(10);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + tokenMecanico)
                .body("{}")
                .when().delete("/ordem-servico/" + ordemId + "/insumos/" + insumoId)
                .then().statusCode(400)
                .body("quantidade", Matchers.equalTo("A quantidade deve ser informada"));
    }

    @Test
    void shouldReturn401WhenNoTokenOnDesvincularInsumo() {
        RestAssured.given().contentType("application/json")
                .body("{\"quantidade\":2}")
                .when().delete("/ordem-servico/1/insumos/1")
                .then().statusCode(401);
    }

    // --- erros gerais ---

    @Test
    void shouldReturn422WhenOrdemAbertaExistsForVeiculo() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        String token = obterTokenAtendente();
        String body = String.format("{\"clienteId\":%d,\"veiculoId\":%d,\"descricao\":\"Barulho ao frear\"}", clienteId, veiculoId);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when().post("/ordem-servico")
                .then().statusCode(201);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when().post("/ordem-servico")
                .then().statusCode(422)
                .body("message", Matchers.equalTo("Já existe uma ordem de serviço aberta para este veículo"));
    }

    @Test
    void shouldReturn400WhenClienteIdIsNull() {
        String token = obterTokenAtendente();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"veiculoId\":1,\"descricao\":\"Barulho ao frear\"}")
                .when().post("/ordem-servico")
                .then().statusCode(400)
                .body("clienteId", Matchers.equalTo("O cliente deve ser informado"));
    }

    @Test
    void shouldReturn400WhenVeiculoIdIsNull() {
        String token = obterTokenAtendente();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1,\"descricao\":\"Barulho ao frear\"}")
                .when().post("/ordem-servico")
                .then().statusCode(400)
                .body("veiculoId", Matchers.equalTo("O veículo deve ser informado"));
    }

    @Test
    void shouldReturn400WhenDescricaoIsNull() {
        String token = obterTokenAtendente();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1,\"veiculoId\":1}")
                .when().post("/ordem-servico")
                .then().statusCode(400)
                .body("descricao", Matchers.equalTo("A descrição deve ser informada"));
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        RestAssured.given().contentType("application/json")
                .body("{\"clienteId\":1,\"veiculoId\":1,\"descricao\":\"Barulho ao frear\"}")
                .when().post("/ordem-servico")
                .then().statusCode(401);
    }

    @Test
    void shouldReturn403WhenUserIsNotAtendente() {
        String token = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "admin@test.com");

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1,\"veiculoId\":1,\"descricao\":\"Barulho ao frear\"}")
                .when().post("/ordem-servico")
                .then().statusCode(403);
    }
}
