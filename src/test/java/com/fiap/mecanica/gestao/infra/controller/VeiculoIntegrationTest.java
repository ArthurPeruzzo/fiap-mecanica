package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.infra.gateway.entity.VeiculoEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.VeiculoRepository;
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
class VeiculoIntegrationTest extends AbstractContainer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    private String obterToken() {
        List<RoleEntity> roles = roleRepository.findByNameIn(List.of(RoleEnum.ROLE_ADMINISTRADOR));

        userRepository.saveAndFlush(UserEntity.builder()
                .email("any@any.com")
                .password(securityConfiguration.passwordEncoder().encode("any"))
                .roles(roles)
                .build());

        return RestAssured
                .given()
                .contentType("application/json")
                .body("""
                        {
                            "email": "any@any.com",
                            "password": "any"
                        }
                        """)
                .when()
                .post("/authenticate/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    private Long criarCliente(String token) {
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Pedro","sobrenome":"Silva","cpf":"188.254.690-39"}
                        """)
                .when().post("/cliente").then().statusCode(201);

        return clienteRepository.findAll().getFirst().getId();
    }

    @Test
    void shouldCreateVeiculoSuccessfully() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(String.format(
                        "{\"clienteId\":%d,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}", clienteId))
                .when()
                .post("/veiculo")
                .then()
                .statusCode(201);

        List<VeiculoEntity> veiculos = veiculoRepository.findAll();
        Assertions.assertEquals(1, veiculos.size());
        Assertions.assertEquals("ABC1234", veiculos.getFirst().getPlaca());
        Assertions.assertEquals(clienteId, veiculos.getFirst().getCliente().getId());
    }

    @Test
    void shouldCreateVeiculoWithMercosulPlaca() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(String.format(
                        "{\"clienteId\":%d,\"placa\":\"ABC1D23\",\"modelo\":\"Onix\",\"ano\":2023}", clienteId))
                .when()
                .post("/veiculo")
                .then()
                .statusCode(201);

        Assertions.assertEquals("ABC1D23", veiculoRepository.findAll().getFirst().getPlaca());
    }

    @Test
    void shouldCreateVeiculoWithHyphenatedPlaca() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(String.format(
                        "{\"clienteId\":%d,\"placa\":\"ABC-1234\",\"modelo\":\"Gol\",\"ano\":2020}", clienteId))
                .when()
                .post("/veiculo")
                .then()
                .statusCode(201);

        Assertions.assertEquals("ABC1234", veiculoRepository.findAll().getFirst().getPlaca());
    }

    @Test
    void shouldReturn409WhenPlacaAlreadyExists() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        String body = String.format(
                "{\"clienteId\":%d,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}", clienteId);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(body).when().post("/veiculo").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(body).when().post("/veiculo").then()
                .statusCode(409)
                .body("message", Matchers.equalTo("Já existe um veículo cadastrado com a placa informada"));
    }

    @Test
    void shouldReturn404WhenClienteNotFound() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":9999,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}")
                .when()
                .post("/veiculo")
                .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Cliente não encontrado"));
    }

    @Test
    void shouldReturn400WhenPlacaIsInvalid() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"clienteId\":1,\"placa\":\"INVALIDA\",\"modelo\":\"Gol\",\"ano\":2020}")
                .when()
                .post("/veiculo")
                .then()
                .statusCode(400)
                .body("placa", Matchers.notNullValue());
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        RestAssured
                .given()
                .contentType("application/json")
                .body("{\"clienteId\":1,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}")
                .when()
                .post("/veiculo")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldUpdateVeiculoSuccessfully() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}", clienteId))
                .when().post("/veiculo").then().statusCode(201);

        Long id = veiculoRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"placa\":\"ABC1D23\",\"modelo\":\"Onix\",\"ano\":2023}")
                .when()
                .put("/veiculo/" + id)
                .then()
                .statusCode(204);

        var updated = veiculoRepository.findById(id).orElseThrow();
        Assertions.assertEquals("ABC1D23", updated.getPlaca());
        Assertions.assertEquals("Onix", updated.getModelo());
        Assertions.assertEquals(2023, updated.getAno());
        Assertions.assertEquals(clienteId, updated.getCliente().getId());
    }

    @Test
    void shouldReturn404WhenVeiculoNotFoundOnUpdate() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}")
                .when()
                .put("/veiculo/9999")
                .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Veículo não encontrado"));
    }

    @Test
    void shouldReturn409WhenUpdatingToExistingPlaca() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}", clienteId))
                .when().post("/veiculo").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"placa\":\"ABC1D23\",\"modelo\":\"Onix\",\"ano\":2023}", clienteId))
                .when().post("/veiculo").then().statusCode(201);

        Long secondId = veiculoRepository.findAll().stream()
                .filter(v -> "ABC1D23".equals(v.getPlaca()))
                .findFirst().orElseThrow().getId();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"placa\":\"ABC1234\",\"modelo\":\"Onix\",\"ano\":2023}")
                .when()
                .put("/veiculo/" + secondId)
                .then()
                .statusCode(409)
                .body("message", Matchers.equalTo("Já existe um veículo cadastrado com a placa informada"));
    }

    @Test
    void shouldDeleteVeiculoSuccessfully() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}", clienteId))
                .when().post("/veiculo").then().statusCode(201);

        Long id = veiculoRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/veiculo/" + id)
                .then()
                .statusCode(204);

        Assertions.assertTrue(veiculoRepository.findById(id).isEmpty());
    }

    @Test
    void shouldReturn404WhenVeiculoNotFoundOnDelete() {
        String token = obterToken();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/veiculo/9999")
                .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Veículo não encontrado"));
    }

    @Test
    void shouldReturnEmptyPageWhenNoVeiculosCadastrados() {
        String token = obterToken();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/veiculo")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(0))
                .body("totalElements", Matchers.equalTo(0))
                .body("totalPages", Matchers.equalTo(0))
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(10));
    }

    @Test
    void shouldReturnVeiculosPagedAfterCreation() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}", clienteId))
                .when().post("/veiculo").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"placa\":\"ABC1D23\",\"modelo\":\"Onix\",\"ano\":2023}", clienteId))
                .when().post("/veiculo").then().statusCode(201);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/veiculo")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(2))
                .body("totalElements", Matchers.equalTo(2))
                .body("totalPages", Matchers.equalTo(1))
                .body("content[0].placa", Matchers.notNullValue())
                .body("content[0].modelo", Matchers.notNullValue());
    }

    @Test
    void shouldRespectPageSizeInPagination() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}", clienteId))
                .when().post("/veiculo").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"placa\":\"ABC1D23\",\"modelo\":\"Onix\",\"ano\":2023}", clienteId))
                .when().post("/veiculo").then().statusCode(201);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/veiculo")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(1))
                .body("totalElements", Matchers.equalTo(2))
                .body("totalPages", Matchers.equalTo(2))
                .body("size", Matchers.equalTo(1));
    }

    @Test
    void shouldReturnPlacaFormatadaInResponse() {
        String token = obterToken();
        Long clienteId = criarCliente(token);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body(String.format("{\"clienteId\":%d,\"placa\":\"ABC1234\",\"modelo\":\"Gol\",\"ano\":2020}", clienteId))
                .when().post("/veiculo").then().statusCode(201);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/veiculo")
                .then()
                .statusCode(200)
                .body("content[0].placa", Matchers.equalTo("ABC-1234"));
    }
}
