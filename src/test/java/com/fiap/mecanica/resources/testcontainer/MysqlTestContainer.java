package com.fiap.mecanica.resources.testcontainer;

import org.testcontainers.containers.MySQLContainer;

public class MysqlTestContainer extends MySQLContainer<MysqlTestContainer> {

    private static final String IMAGE_VERSION = "mysql:8.4.8";

    public MysqlTestContainer() {
        super(IMAGE_VERSION);
        super.withDatabaseName("mecanica");
        super.withUsername("root");
        super.withPassword("root");
    }
}
