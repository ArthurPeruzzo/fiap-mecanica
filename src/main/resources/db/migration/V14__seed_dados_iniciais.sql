-- ==============================================================
-- V14__seed_dados_iniciais.sql
-- Carga inicial: clientes, veículos, peças, insumos, serviços
-- e ordens de serviço em todos os status possíveis do sistema.
-- ==============================================================

-- ==============================================================
-- CLIENTES
-- ==============================================================

INSERT INTO `cliente` (`nome`, `sobrenome`, `cpf`, `cnpj`)
VALUES ('Carlos Eduardo', 'Ferreira', '52998224725', NULL);

INSERT INTO `cliente` (`nome`, `sobrenome`, `cpf`, `cnpj`)
VALUES ('Ana Paula', 'Costa', '11144477735', NULL);

INSERT INTO `cliente` (`nome`, `sobrenome`, `cpf`, `cnpj`)
VALUES ('Roberto Alves', 'Moreira', '87748248800', NULL);

INSERT INTO `cliente` (`nome`, `sobrenome`, `cpf`, `cnpj`)
VALUES ('Transportes Veloz', 'Ltda', NULL, 'G9NTLNHH000135');

INSERT INTO `cliente` (`nome`, `sobrenome`, `cpf`, `cnpj`)
VALUES ('Maria Lucia', 'Santos', '07132567891', NULL);

-- ==============================================================
-- VEÍCULOS
-- ==============================================================

INSERT INTO `veiculo` (`cliente_id`, `placa`, `modelo`, `ano`)
VALUES ((SELECT id FROM `cliente` WHERE cpf = '52998224725'), 'ABC1234', 'Honda Civic', 2019);

INSERT INTO `veiculo` (`cliente_id`, `placa`, `modelo`, `ano`)
VALUES ((SELECT id FROM `cliente` WHERE cpf = '52998224725'), 'DEF5678', 'Toyota Corolla', 2021);

INSERT INTO `veiculo` (`cliente_id`, `placa`, `modelo`, `ano`)
VALUES ((SELECT id FROM `cliente` WHERE cpf = '11144477735'), 'GHI9012', 'Ford Ka', 2018);

INSERT INTO `veiculo` (`cliente_id`, `placa`, `modelo`, `ano`)
VALUES ((SELECT id FROM `cliente` WHERE cpf = '11144477735'), 'JKL3456', 'Chevrolet Onix', 2022);

INSERT INTO `veiculo` (`cliente_id`, `placa`, `modelo`, `ano`)
VALUES ((SELECT id FROM `cliente` WHERE cnpj = '11222333000181'), 'MNO7890', 'Fiat Ducato', 2020);

INSERT INTO `veiculo` (`cliente_id`, `placa`, `modelo`, `ano`)
VALUES ((SELECT id FROM `cliente` WHERE cnpj = '11222333000181'), 'PQR1234', 'Mercedes Sprinter', 2021);

INSERT INTO `veiculo` (`cliente_id`, `placa`, `modelo`, `ano`)
VALUES ((SELECT id FROM `cliente` WHERE cpf = '87748248800'), 'STU5678', 'Volkswagen Gol', 2017);

INSERT INTO `veiculo` (`cliente_id`, `placa`, `modelo`, `ano`)
VALUES ((SELECT id FROM `cliente` WHERE cpf = '87748248800'), 'VWX9012', 'Renault Sandero', 2020);

-- ==============================================================
-- PEÇAS
-- Quantidades já refletem o estado atual do estoque, considerando
-- o consumo das ordens de serviço ativas e concluídas abaixo:
--   Filtro de Ar:          60 iniciais - 3 usados (OS3, OS6, OS8)    = 57
--   Correia Dentada:       30 iniciais - 1 usado  (OS4)               = 29
--   Amortecedor Traseiro:  20 iniciais - 1 usado  (OS6)               = 19
--   Pastilha de Freio:     50 iniciais - 4 usados (OS7)               = 46
--   Disco de Freio:        25 iniciais - 2 usados (OS7)               = 23
--   OS5 (CANCELADA): estoque devolvido pelo sistema ao cancelar.
-- ==============================================================

INSERT INTO `peca` (`nome`, `descricao`, `preco`, `quantidade_estoque`)
VALUES
    ('Pastilha de Freio Dianteira', 'Pastilha cerâmica de alta performance para eixo dianteiro',           89.90,  46),
    ('Correia Dentada',             'Correia dentada reforçada compatível com motores 1.0 a 2.0',         145.00,  29),
    ('Amortecedor Traseiro',        'Amortecedor a gás de dupla ação para eixo traseiro',                 320.00,  19),
    ('Vela de Ignição',             'Vela iridium de alto desempenho para motores flex',                   35.50, 100),
    ('Filtro de Ar',                'Filtro de ar esportivo lavável de alta filtragem',                    42.00,  57),
    ('Disco de Freio Dianteiro',    'Disco ventilado para eixo dianteiro, compatível com veículos 1.0-2.0', 210.00, 23);

-- ==============================================================
-- INSUMOS
-- Quantidades já refletem o estado atual do estoque:
--   Óleo de Motor 5W30: 200 iniciais - 13 usados (OS3=4L, OS6=5L, OS8=4L) = 187
-- ==============================================================

INSERT INTO `insumo` (`nome`, `descricao`, `preco`, `quantidade_estoque`, `unidade_medida`)
VALUES
    ('Óleo de Motor 5W30',        'Óleo sintético para motor a gasolina e flex',              38.00, 187, 'LITRO'),
    ('Fluido de Freio DOT4',      'Fluido de alta performance para sistemas de freio a disco', 25.00, 5000, 'ML'),
    ('Graxa Automotiva',          'Graxa multiuso para articulações e rolamentos',             15.00,  80, 'UNIDADE'),
    ('Líquido de Arrefecimento',  'Líquido refrigerante orgânico para radiador e bloco',      22.00, 150, 'LITRO');

-- ==============================================================
-- SERVIÇOS
-- ==============================================================

INSERT INTO `servico` (`nome`, `descricao`, `preco`)
VALUES
    ('Alinhamento e Balanceamento', 'Alinhamento direcional e balanceamento das 4 rodas',              120.00),
    ('Troca de Óleo e Filtros',     'Substituição do óleo do motor e filtros de óleo e ar',             80.00),
    ('Revisão de Freios',           'Inspeção e substituição de pastilhas, discos e fluido de freio',  150.00),
    ('Revisão Completa',            'Revisão geral com mais de 50 itens inspecionados',                350.00),
    ('Troca de Correia Dentada',    'Substituição da correia dentada, tensor e correia dos acessórios', 200.00);

-- ==============================================================
-- VARIÁVEIS AUXILIARES PARA AS ORDENS DE SERVIÇO
-- ==============================================================

SET @atendente_id = (SELECT a.id FROM `atendente` a
                     INNER JOIN `users` u ON a.user_id = u.id
                     WHERE u.email = 'atendente@mecanica.com');

SET @mecanico_id  = (SELECT m.id FROM `mecanico` m
                     INNER JOIN `users` u ON m.user_id = u.id
                     WHERE u.email = 'mecanico@mecanica.com');

SET @cliente_carlos  = (SELECT id FROM `cliente` WHERE cpf  = '52998224725');
SET @cliente_ana     = (SELECT id FROM `cliente` WHERE cpf  = '11144477735');
SET @cliente_roberto = (SELECT id FROM `cliente` WHERE cpf  = '87748248800');
SET @cliente_transp  = (SELECT id FROM `cliente` WHERE cnpj = '11222333000181');

SET @v_civic    = (SELECT id FROM `veiculo` WHERE placa = 'ABC1234');
SET @v_corolla  = (SELECT id FROM `veiculo` WHERE placa = 'DEF5678');
SET @v_ka       = (SELECT id FROM `veiculo` WHERE placa = 'GHI9012');
SET @v_onix     = (SELECT id FROM `veiculo` WHERE placa = 'JKL3456');
SET @v_ducato   = (SELECT id FROM `veiculo` WHERE placa = 'MNO7890');
SET @v_sprinter = (SELECT id FROM `veiculo` WHERE placa = 'PQR1234');
SET @v_gol      = (SELECT id FROM `veiculo` WHERE placa = 'STU5678');
SET @v_sandero  = (SELECT id FROM `veiculo` WHERE placa = 'VWX9012');

SET @s_alinhamento      = (SELECT id FROM `servico` WHERE nome = 'Alinhamento e Balanceamento');
SET @s_troca_oleo       = (SELECT id FROM `servico` WHERE nome = 'Troca de Óleo e Filtros');
SET @s_revisao_freios   = (SELECT id FROM `servico` WHERE nome = 'Revisão de Freios');
SET @s_revisao_completa = (SELECT id FROM `servico` WHERE nome = 'Revisão Completa');
SET @s_correia_dentada  = (SELECT id FROM `servico` WHERE nome = 'Troca de Correia Dentada');

SET @p_pastilha    = (SELECT id FROM `peca` WHERE nome = 'Pastilha de Freio Dianteira');
SET @p_correia     = (SELECT id FROM `peca` WHERE nome = 'Correia Dentada');
SET @p_amortecedor = (SELECT id FROM `peca` WHERE nome = 'Amortecedor Traseiro');
SET @p_filtro_ar   = (SELECT id FROM `peca` WHERE nome = 'Filtro de Ar');
SET @p_disco       = (SELECT id FROM `peca` WHERE nome = 'Disco de Freio Dianteiro');

SET @i_oleo = (SELECT id FROM `insumo` WHERE nome = 'Óleo de Motor 5W30');

-- ==============================================================
-- OS 1 — RECEBIDA
-- Honda Civic de Carlos Ferreira.
-- Cliente relatou barulho ao frear nas curvas. OS recém-aberta,
-- aguardando atribuição de mecânico e início do diagnóstico.
-- ==============================================================

INSERT INTO `ordem_servico`
    (cliente_id, veiculo_id, atendente_id, mecanico_id, status, descricao, data_criacao)
VALUES
    (@cliente_carlos, @v_civic, @atendente_id, NULL, 'RECEBIDA',
     'Cliente relata barulho metálico ao frear nas curvas. Possível desgaste nas pastilhas dianteiras.',
     '2026-04-28 09:15:00');

-- ==============================================================
-- OS 2 — EM_DIAGNOSTICO
-- Toyota Corolla de Carlos Ferreira.
-- Mecânico iniciou o diagnóstico de perda de potência e fumaça
-- azulada no escapamento. Revisão completa já vinculada.
-- ==============================================================

INSERT INTO `ordem_servico`
    (cliente_id, veiculo_id, atendente_id, mecanico_id, status, descricao,
     data_criacao, data_inicio_diagnostico)
VALUES
    (@cliente_carlos, @v_corolla, @atendente_id, @mecanico_id, 'EM_DIAGNOSTICO',
     'Perda de potência progressiva e fumaça azulada no escapamento. Veículo com 65.000 km.',
     '2026-04-25 10:00:00', '2026-04-25 14:30:00');

SET @os2_id = LAST_INSERT_ID();

INSERT INTO `ordem_servico_servico` (ordem_servico_id, servico_id, preco, status)
VALUES (@os2_id, @s_revisao_completa, 350.00, 'NAO_INICIADO');

-- ==============================================================
-- OS 3 — DIAGNOSTICO_CONCLUIDO
-- Ford Ka de Ana Paula Costa.
-- Revisão periódica de 30.000 km concluída pelo mecânico.
-- Orçamento calculado: R$ 394,00
--   Serviços: Troca de Óleo (R$80) + Alinhamento (R$120) = R$200
--   Peças:    Filtro de Ar 1x R$42                        =  R$42
--   Insumos:  Óleo 5W30 4L x R$38                        = R$152
-- ==============================================================

INSERT INTO `ordem_servico`
    (cliente_id, veiculo_id, atendente_id, mecanico_id, status, descricao,
     data_criacao, data_inicio_diagnostico, data_conclusao_diagnostico, orcamento_total)
VALUES
    (@cliente_ana, @v_ka, @atendente_id, @mecanico_id, 'DIAGNOSTICO_CONCLUIDO',
     'Revisão periódica de 30.000 km. Cliente solicita verificação geral do veículo.',
     '2026-04-20 08:00:00', '2026-04-21 09:00:00', '2026-04-22 11:00:00', 394.00);

SET @os3_id = LAST_INSERT_ID();

INSERT INTO `ordem_servico_servico` (ordem_servico_id, servico_id, preco, status)
VALUES
    (@os3_id, @s_troca_oleo,    80.00, 'NAO_INICIADO'),
    (@os3_id, @s_alinhamento,  120.00, 'NAO_INICIADO');

INSERT INTO `ordem_servico_peca` (ordem_servico_id, peca_id, quantidade, preco)
VALUES (@os3_id, @p_filtro_ar, 1, 42.00);

INSERT INTO `ordem_servico_insumo` (ordem_servico_id, insumo_id, quantidade, preco)
VALUES (@os3_id, @i_oleo, 4, 38.00);

-- ==============================================================
-- OS 4 — AGUARDANDO_APROVACAO
-- Chevrolet Onix de Ana Paula Costa.
-- Troca preventiva de correia dentada nos 60.000 km.
-- Orçamento enviado ao cliente, aguardando resposta.
-- Orçamento calculado: R$ 345,00
--   Serviços: Troca de Correia (R$200)  = R$200
--   Peças:    Correia Dentada 1x R$145  = R$145
-- ==============================================================

INSERT INTO `ordem_servico`
    (cliente_id, veiculo_id, atendente_id, mecanico_id, status, descricao,
     data_criacao, data_inicio_diagnostico, data_conclusao_diagnostico,
     data_envio_orcamento, orcamento_total)
VALUES
    (@cliente_ana, @v_onix, @atendente_id, @mecanico_id, 'AGUARDANDO_APROVACAO',
     'Troca preventiva de correia dentada. Veículo com 60.000 km, prazo recomendado pelo fabricante.',
     '2026-04-15 09:00:00', '2026-04-15 13:00:00', '2026-04-16 10:00:00',
     '2026-04-16 10:30:00', 345.00);

SET @os4_id = LAST_INSERT_ID();

INSERT INTO `ordem_servico_servico` (ordem_servico_id, servico_id, preco, status)
VALUES (@os4_id, @s_correia_dentada, 200.00, 'NAO_INICIADO');

INSERT INTO `ordem_servico_peca` (ordem_servico_id, peca_id, quantidade, preco)
VALUES (@os4_id, @p_correia, 1, 145.00);

-- ==============================================================
-- OS 5 — CANCELADA
-- Fiat Ducato de Transportes Veloz.
-- Cliente recusou o orçamento de reparo na suspensão dianteira.
-- Estoque das peças foi devolvido ao cancelar.
-- Orçamento calculado: R$ 329,80
--   Serviços: Revisão de Freios (R$150)    = R$150
--   Peças:    Pastilha Dianteira 2x R$89,90 = R$179,80
-- ==============================================================

INSERT INTO `ordem_servico`
    (cliente_id, veiculo_id, atendente_id, mecanico_id, status, descricao,
     data_criacao, data_inicio_diagnostico, data_conclusao_diagnostico,
     data_envio_orcamento, orcamento_total, data_cancelamento)
VALUES
    (@cliente_transp, @v_ducato, @atendente_id, @mecanico_id, 'CANCELADA',
     'Barulho metálico na suspensão dianteira ao passar em buracos. Diagnóstico aponta desgaste nas pastilhas.',
     '2026-04-10 08:30:00', '2026-04-10 14:00:00', '2026-04-11 09:00:00',
     '2026-04-11 09:30:00', 329.80, '2026-04-12 16:00:00');

SET @os5_id = LAST_INSERT_ID();

INSERT INTO `ordem_servico_servico` (ordem_servico_id, servico_id, preco, status)
VALUES (@os5_id, @s_revisao_freios, 150.00, 'NAO_INICIADO');

INSERT INTO `ordem_servico_peca` (ordem_servico_id, peca_id, quantidade, preco)
VALUES (@os5_id, @p_pastilha, 2, 89.90);

-- ==============================================================
-- OS 6 — EM_EXECUCAO
-- Mercedes Sprinter de Transportes Veloz.
-- Revisão completa dos 50.000 km aprovada pelo cliente.
-- Revisão Completa em execução; Troca de Óleo ainda não iniciada.
-- Orçamento calculado: R$ 982,00
--   Serviços: Revisão Completa (R$350) + Troca Óleo (R$80)     = R$430
--   Peças:    Filtro de Ar 1x R$42 + Amortecedor 1x R$320      = R$362
--   Insumos:  Óleo 5W30 5L x R$38                              = R$190
-- ==============================================================

INSERT INTO `ordem_servico`
    (cliente_id, veiculo_id, atendente_id, mecanico_id, status, descricao,
     data_criacao, data_inicio_diagnostico, data_conclusao_diagnostico,
     data_envio_orcamento, orcamento_total, data_aprovacao)
VALUES
    (@cliente_transp, @v_sprinter, @atendente_id, @mecanico_id, 'EM_EXECUCAO',
     'Revisão completa dos 50.000 km. Verificar amortecedores traseiros e realizar troca de óleo.',
     '2026-04-05 08:00:00', '2026-04-06 09:00:00', '2026-04-07 11:00:00',
     '2026-04-08 09:00:00', 982.00, '2026-04-09 10:15:00');

SET @os6_id = LAST_INSERT_ID();

INSERT INTO `ordem_servico_servico` (ordem_servico_id, servico_id, preco, status, data_inicio_execucao)
VALUES
    (@os6_id, @s_revisao_completa, 350.00, 'EM_EXECUCAO', '2026-04-09 11:00:00'),
    (@os6_id, @s_troca_oleo,        80.00, 'NAO_INICIADO', NULL);

INSERT INTO `ordem_servico_peca` (ordem_servico_id, peca_id, quantidade, preco)
VALUES
    (@os6_id, @p_filtro_ar,   1, 42.00),
    (@os6_id, @p_amortecedor, 1, 320.00);

INSERT INTO `ordem_servico_insumo` (ordem_servico_id, insumo_id, quantidade, preco)
VALUES (@os6_id, @i_oleo, 5, 38.00);

-- ==============================================================
-- OS 7 — FINALIZADA
-- Volkswagen Gol de Roberto Alves Moreira.
-- Troca de pastilhas e discos concluída. Aguardando retirada.
-- Orçamento calculado: R$ 929,60
--   Serviços: Revisão de Freios (R$150)                        = R$150
--   Peças:    Pastilha 4x R$89,90 (R$359,60) + Disco 2x R$210 = R$779,60
-- ==============================================================

INSERT INTO `ordem_servico`
    (cliente_id, veiculo_id, atendente_id, mecanico_id, status, descricao,
     data_criacao, data_inicio_diagnostico, data_conclusao_diagnostico,
     data_envio_orcamento, orcamento_total, data_aprovacao, data_finalizacao)
VALUES
    (@cliente_roberto, @v_gol, @atendente_id, @mecanico_id, 'FINALIZADA',
     'Desgaste acentuado nas pastilhas e discos dianteiros. Pedal esponjoso e distância de frenagem aumentada.',
     '2026-03-20 08:00:00', '2026-03-21 09:00:00', '2026-03-22 10:00:00',
     '2026-03-23 08:30:00', 929.60, '2026-03-24 09:00:00', '2026-04-02 16:30:00');

SET @os7_id = LAST_INSERT_ID();

INSERT INTO `ordem_servico_servico`
    (ordem_servico_id, servico_id, preco, status, data_inicio_execucao, data_fim_execucao)
VALUES
    (@os7_id, @s_revisao_freios, 150.00, 'FINALIZADO', '2026-03-24 10:00:00', '2026-04-02 16:30:00');

INSERT INTO `ordem_servico_peca` (ordem_servico_id, peca_id, quantidade, preco)
VALUES
    (@os7_id, @p_pastilha, 4, 89.90),
    (@os7_id, @p_disco,    2, 210.00);

-- ==============================================================
-- OS 8 — ENTREGUE
-- Renault Sandero de Roberto Alves Moreira.
-- Manutenção preventiva concluída e veículo entregue ao cliente.
-- Orçamento calculado: R$ 274,00
--   Serviços: Troca de Óleo (R$80)    = R$80
--   Peças:    Filtro de Ar 1x R$42    = R$42
--   Insumos:  Óleo 5W30 4L x R$38    = R$152
-- ==============================================================

INSERT INTO `ordem_servico`
    (cliente_id, veiculo_id, atendente_id, mecanico_id, status, descricao,
     data_criacao, data_inicio_diagnostico, data_conclusao_diagnostico,
     data_envio_orcamento, orcamento_total, data_aprovacao, data_finalizacao, data_entrega)
VALUES
    (@cliente_roberto, @v_sandero, @atendente_id, @mecanico_id, 'ENTREGUE',
     'Manutenção preventiva padrão. Troca de óleo, filtro de ar e verificação geral do veículo.',
     '2026-03-10 09:00:00', '2026-03-11 10:00:00', '2026-03-12 11:00:00',
     '2026-03-13 09:00:00', 274.00, '2026-03-14 10:00:00', '2026-03-25 15:00:00', '2026-03-25 17:00:00');

SET @os8_id = LAST_INSERT_ID();

INSERT INTO `ordem_servico_servico`
    (ordem_servico_id, servico_id, preco, status, data_inicio_execucao, data_fim_execucao)
VALUES
    (@os8_id, @s_troca_oleo, 80.00, 'FINALIZADO', '2026-03-14 11:00:00', '2026-03-25 14:30:00');

INSERT INTO `ordem_servico_peca` (ordem_servico_id, peca_id, quantidade, preco)
VALUES (@os8_id, @p_filtro_ar, 1, 42.00);

INSERT INTO `ordem_servico_insumo` (ordem_servico_id, insumo_id, quantidade, preco)
VALUES (@os8_id, @i_oleo, 4, 38.00);
