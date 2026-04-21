CREATE TABLE IF NOT EXISTS `veiculo` (
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `cliente_id` bigint NOT NULL,
    `placa`      varchar(10) NOT NULL,
    `modelo`     varchar(100) NOT NULL,
    `ano`        int NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_veiculo_placa` (`placa`),
    CONSTRAINT `fk_veiculo_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`)
);
