-- Contexto de trace (cabecalho W3C traceparent) da requisicao que gerou o evento. O relay publica depois, em outra
-- thread e sem requisicao, entao sem esta coluna o trace se perderia na outbox e o consumo no estoque apareceria
-- como um trace separado do cancelamento. NULL = evento gravado sem trace ativo.
ALTER TABLE outbox_evento
    ADD COLUMN traceparent VARCHAR(100) NULL;
