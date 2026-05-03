CREATE TABLE IF NOT EXISTS `ordem_servico_servico` (
    `id`               bigint NOT NULL AUTO_INCREMENT,
    `ordem_servico_id` bigint  NOT NULL,
    `servico_id`       bigint NOT NULL,
    `preco`            decimal(10, 2) NOT NULL,
    `status`           varchar(50) NOT NULL,
    `data_inicio_execucao` datetime NULL,
    `data_fim_execucao` datetime NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oss_ordem_servico` (`ordem_servico_id`, `servico_id`),
    CONSTRAINT `fk_oss_ordem_servico` FOREIGN KEY (`ordem_servico_id`) REFERENCES `ordem_servico` (`id`),
    CONSTRAINT `fk_oss_servico`       FOREIGN KEY (`servico_id`)       REFERENCES `servico`        (`id`)
);

