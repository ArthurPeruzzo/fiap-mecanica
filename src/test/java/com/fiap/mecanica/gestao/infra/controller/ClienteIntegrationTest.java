package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
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
class ClienteIntegrationTest extends AbstractContainer {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private SecurityConfiguration securityConfiguration;

	@Autowired
	private ClienteRepository clienteRepository;

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
				.body("token", Matchers.notNullValue())
				.body("token", Matchers.instanceOf(String.class))
				.extract()
				.path("token");

	}

	@Test
	void shouldCreateClienteSuccessFully() {
		String token = obterToken();

		RestAssured
				.given()
				.contentType("application/json")
				.header("Authorization", "Bearer " + token)
				.body("""
						{
						  "nome": "Pedro",
						  "sobrenome": "Silva",
						  "cpf": "188.254.690-39"
						}
						""")
				.when()
				.post("/cliente")
				.then()
				.statusCode(201);

		List<ClienteEntity> allClientes = clienteRepository.findAll();

		Assertions.assertFalse(allClientes.isEmpty());
		Assertions.assertEquals(1, allClientes.size());
	}

	@Test
	void shouldReturn401WhenNoTokenProvided() {
		RestAssured
				.given()
				.contentType("application/json")
				.body("""
						{
						  "nome": "Pedro",
						  "sobrenome": "Silva",
						  "cpf": "188.254.690-39"
						}
						""")
				.when()
				.post("/cliente")
				.then()
				.statusCode(401);
	}
}
