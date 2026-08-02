package com.fiap.mecanica.estoque.infra.controller;

import com.fiap.mecanica.estoque.infra.gateway.repository.PecaRepository;
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
class PecaIntegrationTest extends AbstractContainer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private PecaRepository pecaRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    private String obterToken() {
        List<RoleEntity> roles = roleRepository.findByNameIn(List.of(RoleEnum.ROLE_ADMINISTRADOR));

        userRepository.saveAndFlush(UserEntity.builder()
                .cpf("52998224725")
                .password(securityConfiguration.passwordEncoder().encode("any"))
                .roles(roles)
                .build());

        return RestAssured
                .given()
                .contentType("application/json")
                .body("""
                        {
                            "cpf": "52998224725",
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

    @Test
    void shouldCreatePecaSuccessfully() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro de óleo","descricao":"Filtro para motor 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when()
                .post("/peca")
                .then()
                .statusCode(201);

        var pecas = pecaRepository.findAll();
        Assertions.assertEquals(1, pecas.size());
        Assertions.assertEquals("Filtro de óleo", pecas.getFirst().getNome());
        Assertions.assertEquals(10, pecas.getFirst().getQuantidadeEstoque());
    }

    @Test
    void shouldCreatePecaWithZeroEstoque() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Parafuso M8","descricao":"Parafuso M8 x 20mm","preco":1.50,"quantidadeEstoque":0}
                        """)
                .when()
                .post("/peca")
                .then()
                .statusCode(201);

        Assertions.assertEquals(0, pecaRepository.findAll().getFirst().getQuantidadeEstoque());
    }

    @Test
    void shouldReturn400WhenNomeIsBlank() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when()
                .post("/peca")
                .then()
                .statusCode(400)
                .body("nome", Matchers.notNullValue());
    }

    @Test
    void shouldReturn400WhenPrecoIsZero() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro","descricao":"Filtro 1.0","preco":0.00,"quantidadeEstoque":10}
                        """)
                .when()
                .post("/peca")
                .then()
                .statusCode(400)
                .body("preco", Matchers.equalTo("O preço deve ser maior que zero"));
    }

    @Test
    void shouldReturn400WhenQuantidadeEstoqueIsNegative() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":-1}
                        """)
                .when()
                .post("/peca")
                .then()
                .statusCode(400)
                .body("quantidadeEstoque", Matchers.equalTo("A quantidade em estoque não pode ser negativa"));
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        RestAssured
                .given()
                .contentType("application/json")
                .body("""
                        {"nome":"Filtro","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when()
                .post("/peca")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldReturnEmptyPageWhenNoPecasCadastradas() {
        String token = obterToken();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/peca")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(0))
                .body("totalElements", Matchers.equalTo(0))
                .body("totalPages", Matchers.equalTo(0))
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(10));
    }

    @Test
    void shouldReturnPecasPagedAfterCreation() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro de óleo","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when().post("/peca").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Vela de ignição","descricao":"Vela NGK","preco":15.00,"quantidadeEstoque":5}
                        """)
                .when().post("/peca").then().statusCode(201);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/peca")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(2))
                .body("totalElements", Matchers.equalTo(2))
                .body("totalPages", Matchers.equalTo(1))
                .body("content[0].nome", Matchers.notNullValue())
                .body("content[0].preco", Matchers.notNullValue());
    }

    @Test
    void shouldRespectPageSizeInPagination() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro de óleo","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when().post("/peca").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Vela de ignição","descricao":"Vela NGK","preco":15.00,"quantidadeEstoque":5}
                        """)
                .when().post("/peca").then().statusCode(201);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/peca")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(1))
                .body("totalElements", Matchers.equalTo(2))
                .body("totalPages", Matchers.equalTo(2))
                .body("size", Matchers.equalTo(1));
    }

    @Test
    void shouldUpdatePecaSuccessfully() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro de óleo","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when().post("/peca").then().statusCode(201);

        Long id = pecaRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro de ar","descricao":"Filtro novo","preco":35.00,"quantidadeEstoque":20}
                        """)
                .when()
                .put("/peca/" + id)
                .then()
                .statusCode(204);

        var updated = pecaRepository.findById(id).orElseThrow();
        Assertions.assertEquals("Filtro de ar", updated.getNome());
        Assertions.assertEquals("Filtro novo", updated.getDescricao());
        Assertions.assertEquals(0, new java.math.BigDecimal("35.00").compareTo(updated.getPreco()));
        Assertions.assertEquals(20, updated.getQuantidadeEstoque());
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnUpdate() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when()
                .put("/peca/9999")
                .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Peça não encontrada"));
    }

    @Test
    void shouldReturn400WhenNomeIsBlankOnUpdate() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when().post("/peca").then().statusCode(201);

        Long id = pecaRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when()
                .put("/peca/" + id)
                .then()
                .statusCode(400)
                .body("nome", Matchers.notNullValue());
    }

    @Test
    void shouldDeletePecaSuccessfully() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Filtro de óleo","descricao":"Filtro 1.0","preco":29.90,"quantidadeEstoque":10}
                        """)
                .when().post("/peca").then().statusCode(201);

        Long id = pecaRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/peca/" + id)
                .then()
                .statusCode(204);

        Assertions.assertTrue(pecaRepository.findById(id).isEmpty());
    }

    @Test
    void shouldReturn404WhenPecaNotFoundOnDelete() {
        String token = obterToken();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/peca/9999")
                .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Peça não encontrada"));
    }
}
