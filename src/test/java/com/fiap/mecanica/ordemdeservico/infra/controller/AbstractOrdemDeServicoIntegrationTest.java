package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.infra.gateway.entity.InsumoEntity;
import com.fiap.mecanica.estoque.infra.gateway.entity.PecaEntity;
import com.fiap.mecanica.estoque.infra.gateway.repository.InsumoRepository;
import com.fiap.mecanica.estoque.infra.gateway.repository.PecaRepository;
import com.fiap.mecanica.gestao.core.domain.Turno;
import com.fiap.mecanica.gestao.infra.gateway.entity.AtendenteEntity;
import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.entity.MecanicoEntity;
import com.fiap.mecanica.gestao.infra.gateway.entity.VeiculoEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.AtendenteRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.MecanicoRepository;
import com.fiap.mecanica.gestao.infra.gateway.repository.VeiculoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.ServicoEntity;
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
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

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
    @Autowired protected ServicoRepository servicoRepository;
    @Autowired protected PecaRepository pecaRepository;
    @Autowired protected InsumoRepository insumoRepository;
    @Autowired protected JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    protected String obterToken(RoleEnum role, String email) {
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

    protected String obterTokenAtendente() {
        String token = obterToken(RoleEnum.ROLE_ATENDENTE, "atendente@test.com");
        UserEntity user = userRepository.findByEmail("atendente@test.com").orElseThrow();
        atendenteRepository.saveAndFlush(AtendenteEntity.builder()
                .nome("João").sobrenome("Silva").turno(Turno.INTEGRAL).userId(user.getId()).build());
        return token;
    }

    protected String obterTokenMecanico() {
        String token = obterToken(RoleEnum.ROLE_MECANICO, "mecanico@test.com");
        UserEntity user = userRepository.findByEmail("mecanico@test.com").orElseThrow();
        mecanicoRepository.saveAndFlush(MecanicoEntity.builder()
                .nome("Carlos").sobrenome("Lima").especialidade("Motor").userId(user.getId()).build());
        return token;
    }

    protected String obterTokenOutroMecanico() {
        String token = obterToken(RoleEnum.ROLE_MECANICO, "mecanico2@test.com");
        UserEntity user = userRepository.findByEmail("mecanico2@test.com").orElseThrow();
        mecanicoRepository.saveAndFlush(MecanicoEntity.builder()
                .nome("Pedro").sobrenome("Costa").especialidade("Freios").userId(user.getId()).build());
        return token;
    }

    protected Long criarClienteERetornarId() {
        return clienteRepository.saveAndFlush(ClienteEntity.builder()
                .nome("Maria").cpf("12345678900").build()).getId();
    }

    protected Long criarVeiculoERetornarId(Long clienteId) {
        return veiculoRepository.saveAndFlush(VeiculoEntity.builder()
                .placa("ABC1234").modelo("Civic").ano(2020)
                .cliente(ClienteEntity.builder().id(clienteId).build()).build()).getId();
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

    protected Long criarServicoERetornarId() {
        return servicoRepository.saveAndFlush(ServicoEntity.builder()
                .nome("Troca de óleo")
                .descricao("Serviço de troca de óleo do motor")
                .preco(new BigDecimal("150.00"))
                .build()).getId();
    }

    protected Long criarPecaERetornarId(Integer quantidadeEstoque) {
        return pecaRepository.saveAndFlush(PecaEntity.builder()
                .nome("Filtro de óleo")
                .descricao("Filtro original")
                .preco(new BigDecimal("45.00"))
                .quantidadeEstoque(quantidadeEstoque)
                .build()).getId();
    }

    protected Long criarInsumoERetornarId(Integer quantidadeEstoque) {
        return insumoRepository.saveAndFlush(InsumoEntity.builder()
                .nome("Óleo motor 5W30")
                .descricao("Óleo sintético")
                .preco(new BigDecimal("35.00"))
                .quantidadeEstoque(quantidadeEstoque)
                .unidadeMedida(UnidadeMedida.LITRO)
                .build()).getId();
    }
}
