CREATE TABLE IF NOT EXISTS `administrador` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `nome` varchar(255) NOT NULL,
    `sobrenome` varchar(255) NOT NULL,
    `user_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_user_administrador` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

-- Senha em texto plano: MeCanica2026!@#
INSERT INTO `users` (`email`, `password`)
VALUES ('administrador@mecanica.com', '$2a$10$AYbJPybutQmQz0ewR0wpb.E6d.S0DwZik2t2hD/koba1UQY10jUdO');

SET @user_id = LAST_INSERT_ID();

INSERT INTO `users_roles` (`user_id`, `role_id`)
SELECT @user_id, id FROM `roles` WHERE `name` = 'ROLE_ADMINISTRADOR';

INSERT INTO `administrador` (`nome`, `sobrenome`, `user_id`)
VALUES ('Cleber', 'Carvalho', @user_id);