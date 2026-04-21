CREATE TABLE IF NOT EXISTS `servico` (
    `id`        bigint         NOT NULL AUTO_INCREMENT,
    `nome`      varchar(255)   NOT NULL,
    `descricao` varchar(255)   NOT NULL,
    `preco`     decimal(10, 2) NOT NULL,
    PRIMARY KEY (`id`)
);
