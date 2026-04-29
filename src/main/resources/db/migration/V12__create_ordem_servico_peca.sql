CREATE TABLE IF NOT EXISTS `ordem_servico_peca` (
    `id`               bigint NOT NULL AUTO_INCREMENT,
    `ordem_servico_id` bigint  NOT NULL,
    `peca_id`          bigint  NOT NULL,
    `quantidade`       int     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_osp_ordem_peca` (`ordem_servico_id`, `peca_id`),
    CONSTRAINT `fk_osp_ordem_servico` FOREIGN KEY (`ordem_servico_id`) REFERENCES `ordem_servico` (`id`),
    CONSTRAINT `fk_osp_peca`          FOREIGN KEY (`peca_id`)          REFERENCES `peca`           (`id`)
);
