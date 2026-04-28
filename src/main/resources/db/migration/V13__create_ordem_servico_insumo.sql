CREATE TABLE IF NOT EXISTS `ordem_servico_insumo` (
    `id`               bigint NOT NULL AUTO_INCREMENT,
    `ordem_servico_id` bigint  NOT NULL,
    `insumo_id`        bigint  NOT NULL,
    `quantidade`       int     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_osi_ordem_insumo` (`ordem_servico_id`, `insumo_id`),
    CONSTRAINT `fk_osi_ordem_servico` FOREIGN KEY (`ordem_servico_id`) REFERENCES `ordem_servico` (`id`),
    CONSTRAINT `fk_osi_insumo`        FOREIGN KEY (`insumo_id`)        REFERENCES `insumo`        (`id`)
);
