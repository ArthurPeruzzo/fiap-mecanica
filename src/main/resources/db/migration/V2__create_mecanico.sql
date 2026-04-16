CREATE TABLE IF NOT EXISTS `mecanico` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `nome` varchar(255) NOT NULL,
    `sobrenome` varchar(255) NOT NULL,
    `especialidade` varchar(255) NOT NULL,
    `user_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_user_mecanico` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

-- Senha em texto plano: MeCanica2026!@#
INSERT INTO `users` (`email`, `password`)
VALUES ('mecanico@mecanica.com', '$2a$10$AYbJPybutQmQz0ewR0wpb.E6d.S0DwZik2t2hD/koba1UQY10jUdO');

SET @user_id = LAST_INSERT_ID();

INSERT INTO `users_roles` (`user_id`, `role_id`)
SELECT @user_id, id FROM `roles` WHERE `name` = 'ROLE_MECANICO';

INSERT INTO `mecanico` (`nome`, `sobrenome`, `especialidade`, `user_id`)
VALUES ('João', 'Silva', 'Motor', @user_id);