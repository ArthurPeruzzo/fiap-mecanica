package com.fiap.mecanica.estoque.infra.controller;

import com.fiap.mecanica.estoque.infra.gateway.repository.InsumoRepository;
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
class InsumoIntegrationTest extends AbstractContainer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private InsumoRepository insumoRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    private String obterToken() {
        List<RoleEntity> roles = roleRepository.findByNameIn(List.of(RoleEnum.ROLE_ATENDENTE));

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
    void shouldCreateInsumoSuccessfully() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Óleo de motor","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when()
                .post("/insumo")
                .then()
                .statusCode(201);

        var insumos = insumoRepository.findAll();
        Assertions.assertEquals(1, insumos.size());
        Assertions.assertEquals("Óleo de motor", insumos.getFirst().getNome());
        Assertions.assertEquals(10, insumos.getFirst().getQuantidadeEstoque());
    }

    @Test
    void shouldCreateInsumoWithAllUnidadeMedidaValues() {
        String token = obterToken();

        for (String unidade : List.of("LITRO", "ML", "UNIDADE")) {
            RestAssured
                    .given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(String.format(
                            "{\"nome\":\"Item %s\",\"descricao\":\"Desc\",\"preco\":10.00,\"quantidadeEstoque\":5,\"unidadeMedida\":\"%s\"}",
                            unidade, unidade))
                    .when()
                    .post("/insumo")
                    .then()
                    .statusCode(201);
        }

        Assertions.assertEquals(3, insumoRepository.findAll().size());
    }

    @Test
    void shouldCreateInsumoWithZeroEstoque() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"Fluido de freio","descricao":"DOT 4","preco":25.00,"quantidadeEstoque":0,"unidadeMedida":"ML"}
                        """)
                .when()
                .post("/insumo")
                .then()
                .statusCode(201);

        Assertions.assertEquals(0, insumoRepository.findAll().getFirst().getQuantidadeEstoque());
    }

    @Test
    void shouldReturn400WhenNomeIsBlank() {
        String token = obterToken();

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {"nome":"","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when()
                .post("/insumo")
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
                        {"nome":"Óleo","descricao":"Óleo 5W30","preco":0.00,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when()
                .post("/insumo")
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
                        {"nome":"Óleo","descricao":"Óleo 5W30","preco":45.90,"quantidadeEstoque":10,"unidadeMedida":"LITRO"}
                        """)
                .when()
                .post("/insumo")
                .then()
                .statusCode(401);
    }
}
