-- Outbox transacional: a intencao de enviar uma mensagem ao broker e gravada na mesma transacao que
-- altera o estado da ordem de servico; o relay publica depois (enviado_em IS NULL = pendente).
CREATE TABLE IF NOT EXISTS outbox_evento (
    id           CHAR(36)     NOT NULL,
    tipo         VARCHAR(60)  NOT NULL,
    agregado_id  BIGINT       NOT NULL,
    payload      VARCHAR(2000) NOT NULL,
    criado_em    DATETIME(3)  NOT NULL,
    enviado_em   DATETIME(3)  NULL,
    tentativas   INT          NOT NULL DEFAULT 0,
    ultimo_erro  VARCHAR(500) NULL,
    PRIMARY KEY (id),
    KEY idx_outbox_pendentes (enviado_em, criado_em)
);
