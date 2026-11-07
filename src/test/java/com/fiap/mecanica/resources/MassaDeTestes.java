package com.fiap.mecanica.resources;

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
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.ServicoRepository;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.infra.config.SecurityConfiguration;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.RoleEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.UserEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.RoleRepository;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.UserRepository;
import io.restassured.RestAssured;

import java.math.BigDecimal;
import java.util.List;

/**
 * Massa de dados dos testes de integração e do BDD: usuários (com login de verdade), cliente, veículo e serviço.
 * É uma classe comum (não é bean) para poder ser usada tanto pela base JUnit quanto pelos passos do Cucumber, que não
 * podem herdar uma classe de teste do Spring. Depende de {@code RestAssured.port} já apontado para a aplicação.
 */
public class MassaDeTestes {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecurityConfiguration securityConfiguration;
    private final AtendenteRepository atendenteRepository;
    private final MecanicoRepository mecanicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;

    public MassaDeTestes(UserRepository userRepository, RoleRepository roleRepository,
                         SecurityConfiguration securityConfiguration, AtendenteRepository atendenteRepository,
                         MecanicoRepository mecanicoRepository, ClienteRepository clienteRepository,
                         VeiculoRepository veiculoRepository, ServicoRepository servicoRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.securityConfiguration = securityConfiguration;
        this.atendenteRepository = atendenteRepository;
        this.mecanicoRepository = mecanicoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
    }

    public String obterToken(RoleEnum role, String cpf) {
        List<RoleEntity> roles = roleRepository.findByNameIn(List.of(role));
        userRepository.saveAndFlush(UserEntity.builder()
                .cpf(cpf)
                .password(securityConfiguration.passwordEncoder().encode("senha123"))
                .roles(roles)
                .build());
        return RestAssured
                .given().contentType("application/json")
                .body(String.format("{\"cpf\":\"%s\",\"password\":\"senha123\"}", cpf))
                .when().post("/authenticate/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    public String obterTokenAtendente() {
        String token = obterToken(RoleEnum.ROLE_ATENDENTE, "44477711107");
        UserEntity user = userRepository.findByCpf("44477711107").orElseThrow();
        atendenteRepository.saveAndFlush(AtendenteEntity.builder()
                .nome("João").sobrenome("Silva").turno(Turno.INTEGRAL).userId(user.getId()).build());
        return token;
    }

    public String obterTokenMecanico() {
        String token = obterToken(RoleEnum.ROLE_MECANICO, "55588822200");
        UserEntity user = userRepository.findByCpf("55588822200").orElseThrow();
        mecanicoRepository.saveAndFlush(MecanicoEntity.builder()
                .nome("Carlos").sobrenome("Lima").especialidade("Motor").userId(user.getId()).build());
        return token;
    }

    public String obterTokenOutroMecanico() {
        String token = obterToken(RoleEnum.ROLE_MECANICO, "88811155576");
        UserEntity user = userRepository.findByCpf("88811155576").orElseThrow();
        mecanicoRepository.saveAndFlush(MecanicoEntity.builder()
                .nome("Pedro").sobrenome("Costa").especialidade("Freios").userId(user.getId()).build());
        return token;
    }

    public Long criarClienteERetornarId() {
        return clienteRepository.saveAndFlush(ClienteEntity.builder()
                .nome("Maria").cpf("12345678900").build()).getId();
    }

    public Long criarVeiculoERetornarId(Long clienteId) {
        return veiculoRepository.saveAndFlush(VeiculoEntity.builder()
                .placa("ABC1234").modelo("Civic").ano(2020)
                .cliente(ClienteEntity.builder().id(clienteId).build()).build()).getId();
    }

    public Long criarServicoERetornarId() {
        return servicoRepository.saveAndFlush(ServicoEntity.builder()
                .nome("Troca de óleo")
                .descricao("Serviço de troca de óleo do motor")
                .preco(new BigDecimal("150.00"))
                .build()).getId();
    }
}
