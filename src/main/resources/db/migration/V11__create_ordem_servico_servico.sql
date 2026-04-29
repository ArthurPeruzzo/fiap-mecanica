CREATE TABLE IF NOT EXISTS `ordem_servico_servico` (
    `ordem_servico_id` bigint NOT NULL,
    `servico_id`       bigint NOT NULL,
    PRIMARY KEY (`ordem_servico_id`, `servico_id`),
    CONSTRAINT `fk_oss_ordem_servico` FOREIGN KEY (`ordem_servico_id`) REFERENCES `ordem_servico` (`id`),
    CONSTRAINT `fk_oss_servico`       FOREIGN KEY (`servico_id`)       REFERENCES `servico`        (`id`)
);
