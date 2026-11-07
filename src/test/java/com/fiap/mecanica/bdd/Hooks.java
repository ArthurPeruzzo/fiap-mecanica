package com.fiap.mecanica.bdd;

import com.fiap.mecanica.resources.BancoDeTestes;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

/** Isola os cenários: banco limpo, estoque simulado sem chamadas anteriores e REST Assured apontado para a aplicação. */
public class Hooks {

    @Autowired private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @Before
    public void prepararCenario() {
        RestAssured.port = port;
        CucumberSpringConfig.estoqueSimulado().resetAll();
        BancoDeTestes.truncarTudo(jdbcTemplate);
    }

    @After
    public void limparCenario() {
        BancoDeTestes.truncarTudo(jdbcTemplate);
    }
}
