package com.fiap.mecanica.resources.testcontainer;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public abstract class AbstractContainer {

    protected static JdbcDatabaseContainer<?> genericContainer = new MysqlTestContainer();

    /**
     * Simula o fiap-estoque. Sobe uma única vez por JVM, como o MySQL, e a URL é a mesma em todos os
     * testes: assim o contexto do Spring continua sendo reaproveitado entre as classes.
     */
    protected static final WireMockServer estoque = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        genericContainer.start();
        estoque.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", genericContainer::getJdbcUrl);
        registry.add("spring.datasource.username", genericContainer::getUsername);
        registry.add("spring.datasource.password", genericContainer::getPassword);
        registry.add("estoque.url", estoque::baseUrl);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanupBeforeTest() {
        estoque.resetAll();
        truncateTables();
    }

    @AfterEach
    public void truncateTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() " +
                        "AND table_name not in('flyway_schema_history', 'roles')",
                String.class
        );

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table));

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
