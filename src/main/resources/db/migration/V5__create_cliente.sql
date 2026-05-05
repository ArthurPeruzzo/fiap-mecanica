CREATE TABLE IF NOT EXISTS `cliente` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `nome` varchar(255) NOT NULL,
    `cnpj` varchar(255) NULL,
    `cpf` varchar(255) NULL,
    PRIMARY KEY (`id`)
);