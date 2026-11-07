package com.fiap.mecanica.ordemdeservico.core.gateway;

import java.math.BigDecimal;

/**
 * Comandos assíncronos enviados ao fiap-cobranca. Como os do estoque, só registram a intenção na transação em
 * curso (outbox); o relay entrega ao broker depois.
 */
public interface PagamentoComandosGateway {

    /** Pede à cobrança que registre o pagamento do orçamento aprovado da ordem de serviço. */
    void solicitarPagamento(Long ordemServicoId, Long clienteId, BigDecimal valor);
}
