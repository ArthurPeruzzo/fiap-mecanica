package com.fiap.mecanica.ordemdeservico.infra.controller;

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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServicoIntegrationTest extends AbstractContainer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private ServicoRepository servicoRepository;

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

    @Test
    void shouldCreateServicoSuccessfully() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Troca de óleo","descricao":"Troca de óleo com filtro incluso","preco":150.00}
                        """)
                .when()
                .post("/servico")
                .then()
                .statusCode(201);

        var servicos = servicoRepository.findAll();
        Assertions.assertEquals(1, servicos.size());
        Assertions.assertEquals("Troca de óleo", servicos.getFirst().getNome());
        Assertions.assertEquals(0, new java.math.BigDecimal("150.00").compareTo(servicos.getFirst().getPreco()));
    }

    @Test
    void shouldReturn400WhenNomeIsBlank() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"","descricao":"Desc","preco":150.00}
                        """)
                .when()
                .post("/servico")
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
                        {"nome":"Troca de óleo","descricao":"Desc","preco":0.00}
                        """)
                .when()
                .post("/servico")
                .then()
                .statusCode(400)
                .body("preco", Matchers.equalTo("O preço deve ser maior que zero"));
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        RestAssured
                .given()
                .contentType("application/json")
                .body("""
                        {"nome":"Troca de óleo","descricao":"Desc","preco":150.00}
                        """)
                .when()
                .post("/servico")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldReturnEmptyPageWhenNoServicosCadastrados() {
        String token = obterToken();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/servico")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(0))
                .body("totalElements", Matchers.equalTo(0))
                .body("totalPages", Matchers.equalTo(0))
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(10));
    }

    @Test
    void shouldReturnServicosPagedAfterCreation() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Troca de óleo","descricao":"Troca com filtro","preco":150.00}
                        """)
                .when().post("/servico").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Alinhamento","descricao":"Alinhamento e balanceamento","preco":120.00}
                        """)
                .when().post("/servico").then().statusCode(201);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/servico")
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
                        {"nome":"Troca de óleo","descricao":"Desc","preco":150.00}
                        """)
                .when().post("/servico").then().statusCode(201);

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Alinhamento","descricao":"Desc","preco":120.00}
                        """)
                .when().post("/servico").then().statusCode(201);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/servico")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(1))
                .body("totalElements", Matchers.equalTo(2))
                .body("totalPages", Matchers.equalTo(2))
                .body("size", Matchers.equalTo(1));
    }

    @Test
    void shouldUpdateServicoSuccessfully() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Troca de óleo","descricao":"Desc","preco":150.00}
                        """)
                .when().post("/servico").then().statusCode(201);

        Long id = servicoRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Alinhamento","descricao":"Alinhamento e balanceamento","preco":200.00}
                        """)
                .when()
                .put("/servico/" + id)
                .then()
                .statusCode(204);

        var updated = servicoRepository.findById(id).orElseThrow();
        Assertions.assertEquals("Alinhamento", updated.getNome());
        Assertions.assertEquals("Alinhamento e balanceamento", updated.getDescricao());
        Assertions.assertEquals(0, new java.math.BigDecimal("200.00").compareTo(updated.getPreco()));
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnUpdate() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Troca de óleo","descricao":"Desc","preco":150.00}
                        """)
                .when()
                .put("/servico/9999")
                .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Serviço não encontrado"));
    }

    @Test
    void shouldReturn400WhenNomeIsBlankOnUpdate() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Troca de óleo","descricao":"Desc","preco":150.00}
                        """)
                .when().post("/servico").then().statusCode(201);

        Long id = servicoRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"","descricao":"Desc","preco":150.00}
                        """)
                .when()
                .put("/servico/" + id)
                .then()
                .statusCode(400)
                .body("nome", Matchers.notNullValue());
    }

    @Test
    void shouldDeleteServicoSuccessfully() {
        String token = obterToken();

        RestAssured.given().contentType("application/json").header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Troca de óleo","descricao":"Desc","preco":150.00}
                        """)
                .when().post("/servico").then().statusCode(201);

        Long id = servicoRepository.findAll().getFirst().getId();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/servico/" + id)
                .then()
                .statusCode(204);

        Assertions.assertTrue(servicoRepository.findById(id).isEmpty());
    }

    @Test
    void shouldReturn404WhenServicoNotFoundOnDelete() {
        String token = obterToken();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/servico/9999")
                .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Serviço não encontrado"));
    }
}
