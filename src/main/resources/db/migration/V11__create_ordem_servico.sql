-- Datas em datetime(6): a duração de cada fase é Duration.between(inicio, fim)
-- e, com precisão de segundo, transições no mesmo segundo davam duração zero
-- (métrica os.duracao zerada na New Relic).
CREATE TABLE IF NOT EXISTS `ordem_servico` (
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `cliente_id`   bigint       NOT NULL,
    `veiculo_id`   bigint       NOT NULL,
    `atendente_id` bigint       NOT NULL,
    `mecanico_id`  bigint       NULL,
    `status`       varchar(50)  NOT NULL,
    `descricao`    varchar(300) NOT NULL,
    `data_criacao` datetime(6)     NOT NULL,
    `data_inicio_diagnostico` datetime(6) NULL,
    `data_conclusao_diagnostico` datetime(6) NULL,
    `data_envio_orcamento` datetime(6) NULL,
    `orcamento_total` decimal(10,2)  NULL,
    `data_cancelamento` datetime(6) NULL,
    `data_aprovacao` datetime(6) NULL,
    `data_finalizacao` datetime(6) NULL,
    `data_entrega` datetime(6) NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_ordem_servico_cliente`  FOREIGN KEY (`cliente_id`)  REFERENCES `cliente`  (`id`),
    CONSTRAINT `fk_ordem_servico_veiculo`  FOREIGN KEY (`veiculo_id`)  REFERENCES `veiculo`  (`id`),
    CONSTRAINT `fk_ordem_servico_atendente` FOREIGN KEY (`atendente_id`) REFERENCES `atendente` (`id`),
    CONSTRAINT `fk_ordem_servico_mecanico` FOREIGN KEY (`mecanico_id`) REFERENCES `mecanico` (`id`)
);
