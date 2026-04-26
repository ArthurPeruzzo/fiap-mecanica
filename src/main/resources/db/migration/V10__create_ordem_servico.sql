CREATE TABLE IF NOT EXISTS `ordem_servico` (
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `cliente_id`   bigint       NOT NULL,
    `veiculo_id`   bigint       NOT NULL,
    `atendente_id` bigint       NOT NULL,
    `mecanico_id`  bigint       NULL,
    `status`       varchar(50)  NOT NULL,
    `data_criacao` datetime     NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_ordem_servico_cliente`  FOREIGN KEY (`cliente_id`)  REFERENCES `cliente`  (`id`),
    CONSTRAINT `fk_ordem_servico_veiculo`  FOREIGN KEY (`veiculo_id`)  REFERENCES `veiculo`  (`id`),
    CONSTRAINT `fk_ordem_servico_atendente` FOREIGN KEY (`atendente_id`) REFERENCES `atendente` (`id`),
    CONSTRAINT `fk_ordem_servico_mecanico` FOREIGN KEY (`mecanico_id`) REFERENCES `mecanico` (`id`)
);
