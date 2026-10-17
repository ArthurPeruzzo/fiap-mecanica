-- ==============================================================
-- V18__reserva_estoque.sql
-- Troca a baixa direta de estoque por reserva.
--   disponível = quantidade_estoque - quantidade_reservada
-- A OS reserva ao vincular, confirma a baixa ao aprovar o orçamento
-- e libera ao cancelar. Sem FK para ordem_servico/peca/insumo: o
-- estoque vai virar um serviço com banco próprio.
-- ==============================================================

ALTER TABLE `peca`   ADD COLUMN `quantidade_reservada` int NOT NULL DEFAULT 0 AFTER `quantidade_estoque`;
ALTER TABLE `insumo` ADD COLUMN `quantidade_reservada` int NOT NULL DEFAULT 0 AFTER `quantidade_estoque`;

CREATE TABLE IF NOT EXISTS `reserva_estoque` (
    `id`               bigint      NOT NULL AUTO_INCREMENT,
    `ordem_servico_id` bigint      NOT NULL,
    `tipo_item`        varchar(20) NOT NULL,
    `item_id`          bigint      NOT NULL,
    `quantidade`       int         NOT NULL,
    `status`           varchar(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reserva_os_item` (`ordem_servico_id`, `tipo_item`, `item_id`),
    KEY `idx_reserva_os_status` (`ordem_servico_id`, `status`)
);

-- --------------------------------------------------------------
-- Dados existentes: OS abertas (antes da aprovação) tiveram o
-- estoque baixado direto. Devolve ao estoque e reserva.
-- --------------------------------------------------------------

UPDATE `peca` p
JOIN (SELECT osp.peca_id, SUM(osp.quantidade) AS qtd
      FROM `ordem_servico_peca` osp
      JOIN `ordem_servico` os ON os.id = osp.ordem_servico_id
      WHERE os.status IN ('RECEBIDA', 'EM_DIAGNOSTICO', 'DIAGNOSTICO_CONCLUIDO', 'AGUARDANDO_APROVACAO')
      GROUP BY osp.peca_id) r ON r.peca_id = p.id
SET p.quantidade_estoque   = p.quantidade_estoque + r.qtd,
    p.quantidade_reservada = p.quantidade_reservada + r.qtd;

UPDATE `insumo` i
JOIN (SELECT osi.insumo_id, SUM(osi.quantidade) AS qtd
      FROM `ordem_servico_insumo` osi
      JOIN `ordem_servico` os ON os.id = osi.ordem_servico_id
      WHERE os.status IN ('RECEBIDA', 'EM_DIAGNOSTICO', 'DIAGNOSTICO_CONCLUIDO', 'AGUARDANDO_APROVACAO')
      GROUP BY osi.insumo_id) r ON r.insumo_id = i.id
SET i.quantidade_estoque   = i.quantidade_estoque + r.qtd,
    i.quantidade_reservada = i.quantidade_reservada + r.qtd;

INSERT INTO `reserva_estoque` (`ordem_servico_id`, `tipo_item`, `item_id`, `quantidade`, `status`)
SELECT osp.ordem_servico_id, 'PECA', osp.peca_id, osp.quantidade,
       CASE WHEN os.status IN ('EM_EXECUCAO', 'FINALIZADA', 'ENTREGUE') THEN 'CONFIRMADA' ELSE 'ATIVA' END
FROM `ordem_servico_peca` osp
JOIN `ordem_servico` os ON os.id = osp.ordem_servico_id
WHERE os.status <> 'CANCELADA';

INSERT INTO `reserva_estoque` (`ordem_servico_id`, `tipo_item`, `item_id`, `quantidade`, `status`)
SELECT osi.ordem_servico_id, 'INSUMO', osi.insumo_id, osi.quantidade,
       CASE WHEN os.status IN ('EM_EXECUCAO', 'FINALIZADA', 'ENTREGUE') THEN 'CONFIRMADA' ELSE 'ATIVA' END
FROM `ordem_servico_insumo` osi
JOIN `ordem_servico` os ON os.id = osi.ordem_servico_id
WHERE os.status <> 'CANCELADA';
