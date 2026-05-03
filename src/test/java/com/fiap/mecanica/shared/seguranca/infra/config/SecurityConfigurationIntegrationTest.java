package com.fiap.mecanica.shared.seguranca.infra.config;

import com.fiap.mecanica.resources.testcontainer.AbstractContainer;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.RoleEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.UserEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.RoleRepository;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigurationIntegrationTest extends AbstractContainer {

    @LocalServerPort
    private int port;

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SecurityConfiguration securityConfiguration;

    private String tokenAdministrador;
    private String tokenAtendente;
    private String tokenMecanico;

    @BeforeEach
    void configurar() {
        RestAssured.port = port;
        tokenAdministrador = obterToken(RoleEnum.ROLE_ADMINISTRADOR, "admin@test.com");
        tokenAtendente     = obterToken(RoleEnum.ROLE_ATENDENTE,     "atendente@test.com");
        tokenMecanico      = obterToken(RoleEnum.ROLE_MECANICO,      "mecanico@test.com");
    }

    private String obterToken(RoleEnum role, String email) {
        List<RoleEntity> roles = roleRepository.findByNameIn(List.of(role));
        userRepository.saveAndFlush(UserEntity.builder()
                .email(email)
                .password(securityConfiguration.passwordEncoder().encode("senha123"))
                .roles(roles)
                .build());
        return RestAssured.given()
                .contentType("application/json")
                .body(String.format("{\"email\":\"%s\",\"password\":\"senha123\"}", email))
                .when().post("/authenticate/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    // -------------------------------------------------------
    // ADMINISTRADOR — acesso permitido
    // -------------------------------------------------------

    @Test
    void shouldAdministradorAcessarCliente() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAdministrador)
                .when().get("/cliente")
                .then().statusCode(200);
    }

    @Test
    void shouldAdministradorAcessarVeiculo() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAdministrador)
                .when().get("/veiculo")
                .then().statusCode(200);
    }

    @Test
    void shouldAdministradorAcessarServico() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAdministrador)
                .when().get("/servico")
                .then().statusCode(200);
    }

    @Test
    void shouldAdministradorAcessarInsumo() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAdministrador)
                .when().get("/insumo")
                .then().statusCode(200);
    }

    @Test
    void shouldAdministradorAcessarPeca() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAdministrador)
                .when().get("/peca")
                .then().statusCode(200);
    }

    // -------------------------------------------------------
    // ATENDENTE — acesso permitido
    // -------------------------------------------------------

    @Test
    void shouldAtendenteAcessarCriarOrdem() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldAtendenteAcessarEnviarOrcamento() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/envio/999")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldAtendenteAcessarRecusarOrcamento() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/recusar/999")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldAtendenteAcessarAprovarOrcamento() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().post("/ordem-servico/orcamento/aprovar/999")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldAtendenteAcessarEntregar() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().patch("/ordem-servico/999/entregar")
                .then().statusCode(not(equalTo(403)));
    }

    // -------------------------------------------------------
    // MECANICO — acesso permitido
    // -------------------------------------------------------

    @Test
    void shouldMecanicoAcessarIniciarDiagnostico() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/999/diagnostico")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldMecanicoAcessarConcluirDiagnostico() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/999/diagnostico/conclusao")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldMecanicoAcessarVincularServico() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().put("/ordem-servico/999/servicos/999")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldMecanicoAcessarDesvincularServico() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().delete("/ordem-servico/999/servicos/999")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldMecanicoAcessarIniciarServico() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/999/servicos/999/iniciar")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldMecanicoAcessarFinalizarServico() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/999/servicos/999/finalizar")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldMecanicoAcessarVincularPeca() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .contentType("application/json").body("{\"quantidade\":1}")
                .when().put("/ordem-servico/999/pecas/999")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldMecanicoAcessarDesvincularPeca() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .contentType("application/json").body("{\"quantidade\":1}")
                .when().delete("/ordem-servico/999/pecas/999")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldMecanicoAcessarVincularInsumo() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .contentType("application/json").body("{\"quantidade\":1}")
                .when().put("/ordem-servico/999/insumos/999")
                .then().statusCode(not(equalTo(403)));
    }

    @Test
    void shouldMecanicoAcessarDesvincularInsumo() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .contentType("application/json").body("{\"quantidade\":1}")
                .when().delete("/ordem-servico/999/insumos/999")
                .then().statusCode(not(equalTo(403)));
    }

    // -------------------------------------------------------
    // ATENDENTE não pode acessar endpoints do MECANICO
    // -------------------------------------------------------

    @Test
    void shouldReturn403QuandoAtendenteAcessaEndpointsDoMecanico() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().patch("/ordem-servico/999/diagnostico")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().patch("/ordem-servico/999/diagnostico/conclusao")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().put("/ordem-servico/999/servicos/999")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().delete("/ordem-servico/999/servicos/999")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().patch("/ordem-servico/999/servicos/999/iniciar")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().patch("/ordem-servico/999/servicos/999/finalizar")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().put("/ordem-servico/999/pecas/999")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().delete("/ordem-servico/999/pecas/999")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().put("/ordem-servico/999/insumos/999")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().delete("/ordem-servico/999/insumos/999")
                .then().statusCode(403);
    }

    // -------------------------------------------------------
    // MECANICO não pode acessar endpoints do ATENDENTE
    // -------------------------------------------------------

    @Test
    void shouldReturn403QuandoMecanicoAcessaEndpointsDoAtendente() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().post("/ordem-servico")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().post("/ordem-servico/orcamento/envio/999")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().post("/ordem-servico/orcamento/recusar/999")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().post("/ordem-servico/orcamento/aprovar/999")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().patch("/ordem-servico/999/entregar")
                .then().statusCode(403);
    }

    // -------------------------------------------------------
    // ATENDENTE e MECANICO não podem acessar endpoints do ADMINISTRADOR
    // -------------------------------------------------------

    @Test
    void shouldReturn403QuandoAtendenteAcessaEndpointsDoAdministrador() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().get("/cliente")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAtendente)
                .when().get("/peca")
                .then().statusCode(403);
    }

    @Test
    void shouldReturn403QuandoMecanicoAcessaEndpointsDoAdministrador() {
        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().get("/cliente")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenMecanico)
                .when().get("/servico")
                .then().statusCode(403);
    }

    // -------------------------------------------------------
    // ADMINISTRADOR não pode acessar endpoints de ATENDENTE nem MECANICO
    // -------------------------------------------------------

    @Test
    void shouldReturn403QuandoAdministradorAcessaEndpointsDaOrdemDeServico() {
        RestAssured.given().header("Authorization", "Bearer " + tokenAdministrador)
                .when().post("/ordem-servico")
                .then().statusCode(403);

        RestAssured.given().header("Authorization", "Bearer " + tokenAdministrador)
                .when().patch("/ordem-servico/999/diagnostico")
                .then().statusCode(403);
    }
}
