CREATE TABLE IF NOT EXISTS link_aprovacao_orcamento (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    ordem_servico_id BIGINT NOT NULL,
    token            CHAR(36) NOT NULL,
    data_expiracao   DATETIME NOT NULL,
    data_utilizacao  DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_link_ordem_servico (ordem_servico_id),
    UNIQUE KEY uk_link_token (token),
    CONSTRAINT fk_link_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES ordem_servico (id)
);
