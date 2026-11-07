package com.fiap.mecanica.resources;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/** Limpeza do banco entre os testes (JUnit e Cucumber): trunca tudo, menos o histórico do Flyway e os perfis. */
public final class BancoDeTestes {

    private BancoDeTestes() {
    }

    public static void truncarTudo(JdbcTemplate jdbcTemplate) {
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
