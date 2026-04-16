package com.fiap.mecanica.shared.integration;

import com.fiap.mecanica.resources.testcontainer.AbstractContainer;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.infra.config.SecurityConfiguration;
import com.fiap.mecanica.shared.seguranca.infra.controller.AuthenticateController;
import com.fiap.mecanica.shared.seguranca.infra.controller.json.request.LoginRequestJson;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.RoleEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.UserEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.RoleRepository;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticateIntegrationTest extends AbstractContainer {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private AuthenticateController authenticateController;

    @Test
    void shouldAuthenticateSuccessFully() {

        List<RoleEntity> roles = roleRepository.findByNameIn(List.of(RoleEnum.ROLE_ATENDENTE));

        UserEntity user = UserEntity.builder()
                .email("any@any.com")
                .password(securityConfiguration.passwordEncoder().encode("any"))
                .roles(roles)
                .build();

        userRepository.saveAndFlush(user);

        LoginRequestJson requestJson = new LoginRequestJson("any@any.com", "any");
        authenticateController.login(requestJson);

    }
}
