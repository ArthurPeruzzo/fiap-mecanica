package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.gestao.core.domain.Turno;
import com.fiap.mecanica.gestao.infra.gateway.entity.AtendenteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.AtendenteRepository;
import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.gestao.infra.gateway.entity.VeiculoEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.VeiculoRepository;
import com.fiap.mecanica.ordemdeservico.core.domain.StatusOrdemDeServico;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private AtendenteRepository atendenteRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private OrdemDeServicoRepository ordemDeServicoRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    private String obterTokenAtendente() {
        List<RoleEntity> roles = roleRepository.findByNameIn(List.of(RoleEnum.ROLE_ATENDENTE));

        UserEntity user = userRepository.saveAndFlush(UserEntity.builder()
                .email("atendente@test.com")
                .password(securityConfiguration.passwordEncoder().encode("senha123"))
                .roles(roles)
                .build());

        atendenteRepository.saveAndFlush(AtendenteEntity.builder()
                .nome("João")
                .sobrenome("Silva")
                .turno(Turno.INTEGRAL)
                .userId(user.getId())
                .build());

        return RestAssured
                .given()
                .contentType("application/json")
                .body("""
                        {"email":"atendente@test.com","password":"senha123"}
                        """)
                .when()
                .post("/authenticate/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    private Long criarClienteERetornarId() {
        return clienteRepository.saveAndFlush(ClienteEntity.builder()
                .nome("Maria")
                .sobrenome("Santos")
                .cpf("12345678900")
                .build()).getId();
    }

    private Long criarVeiculoERetornarId(Long clienteId) {
        return veiculoRepository.saveAndFlush(VeiculoEntity.builder()
                .placa("ABC1234")
                .modelo("Civic")
                .ano(2020)
                .cliente(ClienteEntity.builder().id(clienteId).build())
                .build()).getId();
    }

    @Test
    void shouldCreateOrdemDeServicoSuccessfully() {
        String token = obterTokenAtendente();
        Long clienteId = criarClienteERetornarId();
        Long veiculoId = criarVeiculoERetornarId(clienteId);

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"veiculoId\":%d}", clienteId, veiculoId))
                .when()
                .post("/ordem-servico")
                .then()
                .statusCode(201);

        var ordens = ordemDeServicoRepository.findAll();
        Assertions.assertEquals(1, ordens.size());
        Assertions.assertEquals(StatusOrdemDeServico.RECEBIDA, ordens.getFirst().getStatus());
        Assertions.assertEquals(clienteId, ordens.getFirst().getClienteId());
        Assertions.assertEquals(veiculoId, ordens.getFirst().getVeiculoId());
        Assertions.assertNotNull(ordens.getFirst().getDataCriacao());
    }

    @Test
    void shouldReturn400WhenClienteIdIsNull() {
        String token = obterTokenAtendente();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"veiculoId\":1}")
                .when()
                .post("/ordem-servico")
                .then()
                .statusCode(400)
                .body("clienteId", Matchers.equalTo("O cliente deve ser informado"));
    }

    @Test
    void shouldReturn400WhenVeiculoIdIsNull() {
        String token = obterTokenAtendente();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1}")
                .when()
                .post("/ordem-servico")
                .then()
                .statusCode(400)
                .body("veiculoId", Matchers.equalTo("O veículo deve ser informado"));
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        RestAssured
                .given()
                .contentType("application/json")
                .body("{\"clienteId\":1,\"veiculoId\":1}")
                .when()
                .post("/ordem-servico")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldReturn403WhenUserIsNotAtendente() {
        List<RoleEntity> roles = roleRepository.findByNameIn(List.of(RoleEnum.ROLE_ADMINISTRADOR));
        userRepository.saveAndFlush(UserEntity.builder()
                .email("admin@test.com")
                .password(securityConfiguration.passwordEncoder().encode("senha123"))
                .roles(roles)
                .build());

        String token = RestAssured
                .given()
                .contentType("application/json")
                .body("""
                        {"email":"admin@test.com","password":"senha123"}
                        """)
                .when()
                .post("/authenticate/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1,\"veiculoId\":1}")
                .when()
                .post("/ordem-servico")
                .then()
                .statusCode(403);
    }
}
