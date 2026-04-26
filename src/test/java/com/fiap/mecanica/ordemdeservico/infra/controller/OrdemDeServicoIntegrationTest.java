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
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoRepository;
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
import org.springframework.test.context.ActiveProfiles;

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
                .body(String.format("{\"clienteId\":%d,\"veiculoId\":%d}", clienteId, veiculoId))
                .when().post("/ordem-servico")
                .then().statusCode(201);
        return ordemDeServicoRepository.findAll().getFirst().getId();
    }

    @Test
    void shouldCreateOrdemDeServicoSuccessfully() {
        String token = obterTokenAtendente();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"veiculoId\":%d}", clienteId, veiculoId))
                .when().post("/ordem-servico")
                .then().statusCode(201);

        var ordens = ordemDeServicoRepository.findAll();
        Assertions.assertEquals(1, ordens.size());
        OrdemDeServicoEntity ordem = ordens.getFirst();
        Assertions.assertEquals(StatusOrdemDeServico.RECEBIDA, ordem.getStatus());
        Assertions.assertEquals(clienteId, ordem.getClienteId());
        Assertions.assertEquals(veiculoId, ordem.getVeiculoId());
        Assertions.assertNotNull(ordem.getDataCriacao());
        Assertions.assertNull(ordem.getDataInicioDiagnostico());
        Assertions.assertNull(ordem.getDataConclusaoDiagnostico());
    }

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

    @Test
    void shouldConcluirDiagnosticoSuccessfully() {
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
                .then().statusCode(204);

        OrdemDeServicoEntity ordem = ordemDeServicoRepository.findById(ordemId).orElseThrow();
        Assertions.assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, ordem.getStatus());
        Assertions.assertNotNull(ordem.getDataInicioDiagnostico());
        Assertions.assertNotNull(ordem.getDataConclusaoDiagnostico());
    }

    @Test
    void shouldReturn422WhenOrdemAbertaExistsForVeiculo() {
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);
        String token = obterTokenAtendente();
        String body = String.format("{\"clienteId\":%d,\"veiculoId\":%d}", clienteId, veiculoId);

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
                .body("{\"veiculoId\":1}")
                .when().post("/ordem-servico")
                .then().statusCode(400)
                .body("clienteId", Matchers.equalTo("O cliente deve ser informado"));
    }

    @Test
    void shouldReturn400WhenVeiculoIdIsNull() {
        String token = obterTokenAtendente();

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1}")
                .when().post("/ordem-servico")
                .then().statusCode(400)
                .body("veiculoId", Matchers.equalTo("O veículo deve ser informado"));
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        RestAssured.given().contentType("application/json")
                .body("{\"clienteId\":1,\"veiculoId\":1}")
                .when().post("/ordem-servico")
                .then().statusCode(401);
    }

    @Test
    void shouldReturn403WhenUserIsNotAtendente() {
        String token = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "admin@test.com");

        RestAssured.given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1,\"veiculoId\":1}")
                .when().post("/ordem-servico")
                .then().statusCode(403);
    }
}
