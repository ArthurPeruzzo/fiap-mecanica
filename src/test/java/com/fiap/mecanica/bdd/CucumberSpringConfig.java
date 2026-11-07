package com.fiap.mecanica.bdd;

import com.fiap.mecanica.resources.testcontainer.AbstractContainer;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Contexto Spring do BDD: o mesmo dos testes de integração (perfil {@code integration-test}, MySQL no Testcontainers e o
 * fiap-estoque simulado em WireMock), subido uma única vez para todos os cenários. Os hooks do JUnit da
 * {@link AbstractContainer} não rodam no Cucumber; a limpeza entre cenários está em {@link Hooks}.
 */
@CucumberContextConfiguration
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfig extends AbstractContainer {

    /** O WireMock do estoque é {@code protected} na base; os passos o alcançam por aqui. */
    public static WireMockServer estoqueSimulado() {
        return estoque;
    }
}
