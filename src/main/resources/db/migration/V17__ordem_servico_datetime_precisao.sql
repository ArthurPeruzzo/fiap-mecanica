-- As datas de fase da OS eram `datetime` (precisao de segundo). A duracao de uma
-- fase e calculada como Duration.between(inicio, fim); quando as duas transicoes
-- ocorrem no mesmo segundo (fluxo rapido / demonstracao) a duracao truncava para
-- zero e a metrica os.duracao ia para a New Relic zerada.
-- datetime(6) preserva microssegundos e mantem a duracao > 0 mesmo em transicoes
-- proximas. Colunas de auditoria (data_criacao, data_envio_orcamento,
-- data_cancelamento) tambem sobem para manter o schema consistente.
ALTER TABLE `ordem_servico`
    MODIFY COLUMN `data_criacao`                datetime(6) NOT NULL,
    MODIFY COLUMN `data_inicio_diagnostico`     datetime(6) NULL,
    MODIFY COLUMN `data_conclusao_diagnostico`  datetime(6) NULL,
    MODIFY COLUMN `data_envio_orcamento`        datetime(6) NULL,
    MODIFY COLUMN `data_cancelamento`           datetime(6) NULL,
    MODIFY COLUMN `data_aprovacao`              datetime(6) NULL,
    MODIFY COLUMN `data_finalizacao`            datetime(6) NULL,
    MODIFY COLUMN `data_entrega`                datetime(6) NULL;

-- Datas de execucao de servico entram no mesmo calculo em
-- calcularTempoMedioExecucaoServicos().
ALTER TABLE `ordem_servico_servico`
    MODIFY COLUMN `data_inicio_execucao` datetime(6) NULL,
    MODIFY COLUMN `data_fim_execucao`    datetime(6) NULL;
